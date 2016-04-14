import java.util.ArrayList;

class RandomForest extends SupervisedLearner
{
	double[] mode;
	int numOfTrees;
	ArrayList<DecisionTree> list;

	RandomForest(){
		this.numOfTrees = 0;
		list = new ArrayList<DecisionTree>();
	}

	RandomForest(int numOfTrees){
		this.numOfTrees = numOfTrees;
		list = new ArrayList<DecisionTree>();
		for(int i = 0; i < this.numOfTrees){
			list.add(new DecisionTree());
		}
	}

	String name()
	{
		return "RandomForest";
	}

	// Should instantiate n DecisionTree instances
	// Generate new training data for each DecisionTree by sampling with replacement. Then train.
	// Give each DecisionTree an equal vote in predictions.
	// For categorical labels, it predicts the value that is most common among the predictions of the models it aggregates. 
	// For continuous labels, it predicts the mean of the predictions of the models it aggregates.
	void train(Matrix features, Matrix labels)
	{
		mode = new double[labels.cols()];
		for(int i = 0; i < labels.cols(); i++)
		{
			if(labels.valueCount(i) == 0)
				mode[i] = labels.columnMean(i);
			else
				mode[i] = labels.mostCommonValue(i);
		}
	}

	void predict(double[] in, double[] out)
	{
		Vec.copy(out, mode);
	}
}
