import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomUniformGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.logging.LoggingConfiguration;
import de.lmu.ifi.dbs.elki.utilities.random.RandomFactory;

// gradle --daemon test --tests "KMeansTest.チュートリアル1"

class KMeansTest extends Specification {

    def チュートリアル1() {
        when:
        // Set the logging level to statistics:
        LoggingConfiguration.setStatistics();

        // Generate a random data set.
        // Note: ELKI has a nice data generator class, use that instead.
        double[][] data = new double[1000][2];
        for(int i = 0; i < data.length; i++) {
          for(int j = 0; j < data[i].length; j++) {
            data[i][j] = Math.random();
          }
        }

        // Adapter to load data from an existing array.
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
        // Create a database (which may contain multiple relations!)
        Database db = new StaticArrayDatabase(dbc, null);
        // Load the data into the database (do NOT forget to initialize...)
        db.initialize();
        // Relation containing the number vectors:
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        // We know that the ids must be a continuous range:
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
          System.out.println("Center: " + clu.getModel().getPrototype().toString());
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

        then:
        1 == 1
    }
}
