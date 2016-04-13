
class RandomForest extends SupervisedLearner
{
	double[] mode;

	String name()
	{
		return "Random Forest: ";
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
