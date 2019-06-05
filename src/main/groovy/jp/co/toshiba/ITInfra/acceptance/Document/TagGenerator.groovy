package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.util.logging.Slf4j
import groovy.transform.ToString
import static groovy.json.JsonOutput.*
import groovy.json.*

// ELKIKMeansClustering
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomUniformGeneratedInitialMeans
import de.lmu.ifi.dbs.elki.data.Cluster
import de.lmu.ifi.dbs.elki.data.Clustering
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.data.model.KMeansModel
import de.lmu.ifi.dbs.elki.data.type.TypeUtil
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange
import de.lmu.ifi.dbs.elki.database.relation.Relation
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.logging.LoggingConfiguration
import de.lmu.ifi.dbs.elki.utilities.random.RandomFactory

import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

@Slf4j
@ToString(includePackage = false)
class DomainCluster {
    def surrogate_keys = [:].withDefault{[:]}
    def dummy_results = [:].withDefault{[:]}
    def index_rows = [:]
    def index_targets = [:]
    def cluster = [:]
    def sorted_clustering_targets = [:]

    def get_surrogate_key(String index, value) {
        def surrogate_key = surrogate_keys[index][value]
        if (surrogate_key == null) {
            def id_max = surrogate_keys[index].size()
            surrogate_key = id_max + 1
        }
        surrogate_keys[index][value] = surrogate_key
        return surrogate_key
    }

    def set_index_row(String index) {
        if (!(index_rows[index])) {
            index_rows[index] = index_rows.size() + 1
        }
    }

    def set_dummy_result(String target_name, String platform_metric, surrogate_key) {
        dummy_results[target_name][platform_metric] = surrogate_key
    }

    // TODO: 変数にn種類のカテゴリがあれば、n個のダミー変数に変換が必要。変数は0,1を指定
    def make_dummy_variables() {
        def colnum = dummy_results.size()
        def rownum = index_rows.size()

        double[][] data = new double[colnum][rownum]
        def col = 0
        dummy_results.each { target_name, dummy_result ->
            index_targets[col] = target_name
            dummy_result.each { platform_metric, surrogate_key ->
                def row = index_rows[platform_metric] - 1
                data[col][row] = (double)surrogate_key
            }
            col ++
        }
        return data
    }

    def set_clustering_data(Map cluster_groups) {
        def target_names = [:]
        cluster_groups.each { cluster_index, cluster_group ->
            def cluster_targets = [:]
            def cluster_target_names = []
            cluster_group.each { node_index ->
                def target_name = index_targets[node_index]
                cluster_targets[node_index] = target_name
                cluster_target_names << target_name
            }
            target_names[cluster_index] = cluster_target_names
            log.info "Clustering result#${cluster_index}: ${cluster_targets}"
        }
        this.sorted_clustering_targets = target_names.sort { 
            a, b -> b.value.size() <=> a.value.size() 
        }
    }
}

@Slf4j
@ToString(includePackage = false)
class TagGenerator {
    def surrogate_keys = [:].withDefault{[:]}
    def dummy_results = [:].withDefault{[:]}
    def index_rows = [:]
    def index_targets = [:]
    int cluster_size = 10
    Map <String,DomainCluster> domain_clusters = new LinkedHashMap<String,DomainCluster>()

    def set_environment(ConfigTestEnvironment env) {
        this.result_dir = env.get_node_dir()
        this.cluster_size = env.get_cluster_size()
    }

    def get_surrogate_key(String index, value) {
        def surrogate_key = surrogate_keys[index][value]
        if (surrogate_key == null) {
            def id_max = surrogate_keys[index].size()
            surrogate_key = id_max + 1
        }
        surrogate_keys[index][value] = surrogate_key
        return surrogate_key
    }

    def set_index_row(String index) {
        if (!(index_rows[index])) {
            index_rows[index] = index_rows.size() + 1
        }
    }

    // TODO: 変数にn種類のカテゴリがあれば、n個のダミー変数に変換が必要。変数は0,1を指定
    def make_dummy_variables() {
        def colnum = dummy_results.size()
        def rownum = index_rows.size()

        double[][] data = new double[colnum][rownum]
        def col = 0
        dummy_results.each { target_name, dummy_result ->
            index_targets[col] = target_name
            dummy_result.each { platform_metric, surrogate_key ->
                def row = index_rows[platform_metric] - 1
                // println "INDEX:$target_name, $platform_metric, $row, $surrogate_key"
                data[col][row] = (double)surrogate_key
            }
            col ++
        }
        return data
    }

    def add_compared_results(String domain, List target_names, TestTargetSet source, TestTargetSet dest) {
        // Add first column to comparison server
        def compare_server = target_names[0]
        def compare_target = source.get(compare_server, domain)
        compare_target.tag = compare_server
        dest.add(compare_target)

        // Add the terget server from the second column
        def target_size = target_names.size()
        (1..(target_size - 1)).each { index ->
            def target_name = target_names[index]
            def test_target = source.get(target_name, domain)
            test_target.compare_server = compare_server
            test_target.tag = compare_server
            dest.add(test_target)
        }
    }

    def add_single_results(String domain, List target_names, TestTargetSet source, TestTargetSet dest) {
        def test_target = source.get(target_names[0], domain)
        test_target.compare_server = null
        dest.add(test_target)
    }

    // TODO: ターゲットにタグの追加、Excelフォームのカラムグループセット
    // TODO: テストシナリオのターゲットリストをクラスターID順にソート
    def make_target_tag(TestScenario test_scenario) {
        def new_test_targets = new TestTargetSet(name: 'cluster')
        def test_targets = test_scenario.test_targets
        domain_clusters.each { domain, domain_cluster ->
            def clustering_targets = domain_cluster.sorted_clustering_targets

            clustering_targets.each { cluster_index, target_names ->
                def target_size = target_names.size()
                if (target_size >= 2) {
                    add_compared_results(domain, target_names, test_targets, new_test_targets)

                } else if (target_size == 1) {
                    add_single_results(domain, target_names, test_targets, new_test_targets)
                }
            }
        }
        test_scenario.test_targets = new_test_targets
        log.info "New sorted clustering targets : ${new_test_targets.get_keys()}"
    }

    def make_surrogate_keys(TestTarget test_target) {
        def domain = test_target.domain
        def target_name = test_target.name
        DomainCluster domain_cluster = this.domain_clusters[domain] ?: new DomainCluster()
        test_target.test_platforms.each { platform_name, test_platform ->
            test_platform?.test_results.each { metric_name, test_result ->
                def platform_metric = "${platform_name}|${metric_name}"
                domain_cluster.set_index_row(platform_metric)

                def value = test_result?.value
                def surrogate_key = domain_cluster.get_surrogate_key(platform_metric, value)
                domain_cluster.set_dummy_result(target_name, platform_metric, surrogate_key)
            }
        }
        this.domain_clusters[domain] = domain_cluster
    }

    def visit_test_scenario(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                this.make_surrogate_keys(test_target)
            }
        }
        log.info("Set Cluster size : ${this.cluster_size}")
        this.domain_clusters.each { domain, domain_cluster ->
            try {
                def data = domain_cluster.make_dummy_variables()
                def cluster_groups = run_elki_kmeans_clustering(data, this.cluster_size)
                domain_cluster.set_clustering_data(cluster_groups)
            } catch (IllegalArgumentException e) {
                log.info "Skip, '${domain}' Clustering analyze : " + e
            }
        }
        make_target_tag(test_scenario)
    }

    Map run_elki_kmeans_clustering(double[][] data, int partitions) {
        LoggingConfiguration.setStatistics();

        def dbc = new ArrayAdapterDatabaseConnection(data);
        def db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        DBIDRange ids = (DBIDRange) rel.getDBIDs();

        // K-means should be used with squared Euclidean (least squares):
        def dist = SquaredEuclideanDistanceFunction.STATIC;
        // Default initialization, using global random:
        // To fix the random seed, use: new RandomFactory(seed);
        // RandomFactory rnd = new RandomFactory(1);
        def init = new RandomUniformGeneratedInitialMeans(RandomFactory.DEFAULT);
        // def init = new RandomUniformGeneratedInitialMeans(rnd);

        // Textbook k-means clustering:
        KMeansLloyd<NumberVector> km = new KMeansLloyd<>(dist, //
            partitions /* k - number of partitions */, //
            0 /* maximum number of iterations: no limit */, init);

        // K-means will automatically choose a numerical relation from the data set:
        // But we could make it explicit (if there were more than one numeric
        // relation!): km.run(db, rel);
        Clustering<KMeansModel> c = km.run(db);

        // Output all clusters:
        def clusters = [:].withDefault{[]}
        int i = 0;
        for(Cluster<KMeansModel> clu : c.getAllClusters()) {
          // K-means will name all clusters "Cluster" in lack of noise support:
          for(DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
            // To get the vector use:
            // NumberVector v = rel.get(it);

            // Offset within our DBID range: "line number"
            final int offset = ids.getOffset(it);
            clusters[i] << offset
            // Do NOT rely on using "internalGetIndex()" directly!
          }
          log.debug "#${i}:${clu.getNameAutomatic()}, ${clusters[i]}"
          ++i;
        }
        return clusters
    }

    def ELKIKMeansClustering() {
        LoggingConfiguration.setStatistics();

        double[][] data = new double[1000][2];
        for(int i = 0; i < data.length; i++) {
          for(int j = 0; j < data[i].length; j++) {
            data[i][j] = Math.random();
          }
        }

        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
        Database db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        DBIDRange ids = (DBIDRange) rel.getDBIDs();

        // K-means should be used with squared Euclidean (least squares):
        SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;
        // Default initialization, using global random:
        // To fix the random seed, use: new RandomFactory(seed);
        // RandomFactory rnd = new RandomFactory(1);
        RandomUniformGeneratedInitialMeans init = new RandomUniformGeneratedInitialMeans(RandomFactory.DEFAULT);
        // RandomUniformGeneratedInitialMeans init = new RandomUniformGeneratedInitialMeans(rnd);

        // Textbook k-means clustering:
        KMeansLloyd<NumberVector> km = new KMeansLloyd<>(dist, //
        3 /* k - number of partitions */, //
        0 /* maximum number of iterations: no limit */, init);

        // K-means will automatically choose a numerical relation from the data set:
        // But we could make it explicit (if there were more than one numeric
        // relation!): km.run(db, rel);
        Clustering<KMeansModel> c = km.run(db);

        // Output all clusters:
        int i = 0;
        for(Cluster<KMeansModel> clu : c.getAllClusters()) {
          // K-means will name all clusters "Cluster" in lack of noise support:
          System.out.println("#" + i + ": " + clu.getNameAutomatic());
          System.out.println("Size: " + clu.size());
          System.out.println("Center0: " + clu.getModel());
          System.out.println("Center1: " + clu.getModel().getPrototype().toString());
          // Iterate over objects:
          System.out.print("Objects: ");
          for(DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
            // To get the vector use:
            // NumberVector v = rel.get(it);

            // Offset within our DBID range: "line number"
            final int offset = ids.getOffset(it);
            System.out.print(" " + offset);
            // Do NOT rely on using "internalGetIndex()" directly!
          }
          System.out.println();
          ++i;
        }
    }
}
