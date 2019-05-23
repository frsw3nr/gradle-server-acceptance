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
class TagGenerator {
    def surrogate_keys = [:].withDefault{[:]}
    def dummy_results = [:].withDefault{[:]}
    def index_rows = [:]
    def index_targets = [:]

    def set_environment(ConfigTestEnvironment env) {
        this.result_dir = env.get_node_dir()
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

    // TODO: ターゲットにタグの追加、Excelフォームのカラムグループセット
    // TODO: テストシナリオのターゲットリストをクラスターID順にソート
    def make_target_tag(Map clusters, TestScenario test_scenario) {
        def compare_servers = [:]
        clusters.find { index, cluster ->
            def target_n = cluster.size()
            if (target_n >= 2) {
                def compare_server = index_targets[cluster[0]]
                (1..(target_n-1)).each { i ->
                    def target_server = index_targets[cluster[i]]
                    compare_servers[target_server] = compare_server
                }
            }
        }
        // println "TARGET_TAG: $compare_servers"
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                def compare_server = compare_servers[target_name]
                log.info "SET TAG: $target_name, $compare_server"
                if (compare_server) {
                    test_target.compare_server = compare_server
                }
            }
        }
    }

    def make_surrogate_keys(TestTarget test_target) {
        test_target.test_platforms.each { platform_name, test_platform ->
            test_platform?.test_results.each { metric_name, test_result ->
                def platform_metric = "${platform_name}|${metric_name}"
                set_index_row(platform_metric)

                def value = test_result?.value
                def surrogate_key = get_surrogate_key(platform_metric, value)
                dummy_results[test_target.name][platform_metric] = surrogate_key
            }
        }
    }

    def visit_test_scenario(TestScenario test_scenario) {
        def targets = test_scenario.test_targets.get_all()
        targets.each { target_name, domain_targets ->
            domain_targets.each { domain, test_target ->
                this.make_surrogate_keys(test_target)
            }
        }
        def data = make_dummy_variables()
        def clusters = run_elki_kmeans_clustering(data, 3)
        make_target_tag(clusters, test_scenario)
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
        RandomFactory rnd = new RandomFactory(1);
        // def init = new RandomUniformGeneratedInitialMeans(RandomFactory.DEFAULT);
        def init = new RandomUniformGeneratedInitialMeans(rnd);

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
          log.info "#${i}:${clu.getNameAutomatic()}, Size: ${clu.size()}, Objects: ${clusters[i]}"
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
