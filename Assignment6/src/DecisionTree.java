abstract class Node
{
	abstract boolean isLeaf();
}

class InteriorNode extends Node
{
	int attribute; // which attribute to divide on
	double pivot; // which value to divide on
	Node a;
	Node b;

	boolean isLeaf() { return false; }
}

class LeafNode extends Node
{
	double[] label;

	boolean isLeaf() { return true; }
}

class DecisionTree extends SupervisedLearner
{
	Node root;

	String name()
	{
		return "Decision Tree Learner: ";
	}

	// Build a tree
	// Training is done by recursively dividing the data until fewer than k samples remain
	// Leaf nodes should store a label vector. Uese the approach that basleine uses to combine the k label vectors into a single label.
	// Tree should handle both continuous and categorical attributes.
	// For categorical labels, it predicts the value that is most common among the predictions of the models it aggregates. 
	// For continuous labels, it predicts the mean of the predictions of the models it aggregates.

	// Interior nodes need only make binary divisions - should store attribute index and a value on which it divides
	// Choose division by picking a random sample and attribute from remaining training data
	void train(Matrix features, Matrix labels)
	{
		mode = new double[labels.cols()];
		for(int i = 0; i < labels.cols(); i++)
		{
			if(labels.valueCount(i) == 0)
				// Continuous Labels
				mode[i] = labels.columnMean(i);
			else
				// Categorical Labels
				mode[i] = labels.mostCommonValue(i);
		}
	}

	// Use the tree (built in train) to make a prediction
	void predict(double[] in, double[] out)
	{
		Vec.copy(out, mode);
	}
}