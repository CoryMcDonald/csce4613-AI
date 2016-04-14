import java.util.ArrayList;
import java.util.Random;

class RandomForest extends SupervisedLearner
{
	double[] mode;
	int numOfTrees;
	ArrayList<DecisionTree> list;
	Random rand;

	RandomForest(){
		this.numOfTrees = 0;
		list = new ArrayList<DecisionTree>();
		rand = new Random(1234);
	}

	RandomForest(int numOfTrees){
		this.numOfTrees = numOfTrees;
		list = new ArrayList<DecisionTree>();
		rand = new Random(1234);
	}

	String name()
	{
		return "RandomForest";
	}

	// Give each DecisionTree an equal vote in predictions.
	// For categorical labels, it predicts the value that is most common among the predictions of the models it aggregates. 
	// For continuous labels, it predicts the mean of the predictions of the models it aggregates.
	void train(Matrix features, Matrix labels)
	{
		for(int i = 0; i < this.numOfTrees; i++){
			DecisionTree temp = new DecisionTree();

			Matrix thisFeatures = new Matrix();
			thisFeatures.copyMetaData(features);
			Matrix thisLabels = new Matrix();
			thisLabels.copyMetaData(labels);

			int randomRow = rand.nextInt(features.rows());

			for(int j = 0; j < features.rows(); j++){
				thisFeatures.newRow();
				thisFeatures.copyBlock(thisFeatures.rows() - 1, 0, features, randomRow, 0, 1, features.cols());
				thisLabels.newRow();
				thisLabels.copyBlock(thisLabels.rows()-1, 0, labels, randomRow, 0, 1, labels.cols());
			}

			temp.train(thisFeatures, thisLabels);
			list.add(temp);
		}
	}

	void predict(double[] in, double[] out)
	{
		Vec.copy(out, mode);
	}
}
