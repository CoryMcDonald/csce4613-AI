import java.util.Random;

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

	LeafNode(Matrix labels){
		label = new double[labels.cols()];
		for(int i = 0; i < labels.cols(); i++)
		{
			if(labels.valueCount(i) == 0)
				label[i] = labels.columnMean(i);
			else
				label[i] = labels.mostCommonValue(i);
		}
	}

	boolean isLeaf() { return true; }
}

class DecisionTree extends SupervisedLearner
{
	double[] mode;
	Node root;
	int minSize;
	Random rand;

	DecisionTree(){
		minSize = 1;
		/// Seed of 1234, chaotic not random
		rand = new Random(1234);
	}

	String name()
	{
		return "DecisionTree";
	}

	Node buildTree(Matrix features, Matrix labels){
		// Chop data until it gets small = leafNode
		/// Base Case
		if(features.rows() != labels.rows())
			throw new IllegalArgumentException("Feature rows doesn't equal label rows.");
		if(features.rows() < minSize){
			return new LeafNode(labels);
		}
		/// Recursive Part
		else{
			/// Cut matrix of features
			int dim = rand.nextInt(features.cols());
			double[] row = features.row(rand.nextInt(features.rows()));
			double val = row[dim];

			Matrix thisFeatures = new Matrix();
			Matrix thisLabels = new Matrix();
			Matrix thatFeatures = new Matrix();
			Matrix thatLabels = new Matrix();

			if(features.valueCount(dim) == 0){
				// Dim is continous
				// All points < val go in otherMatrix
				for(int i = 0; i < features.rows(); i++){
					if(features.row(i)[dim] < val){
						thisFeatures.newColumn();						
						thisFeatures.newRow();
						thisFeatures.copyPart(features, 0, 0, i, thisFeatures.cols());
						thisLabels.newColumn();						
						thisLabels.newRow();
						thisLabels.copyPart(labels, 0, 0, i, thisLabels.cols());
					}
					else{
						thatFeatures.newColumn();						
						thatFeatures.newRow();
						thatFeatures.copyPart(features, 0, 0, i, thatFeatures.cols());
						thatLabels.newColumn();						
						thatLabels.newRow();
						thatLabels.copyPart(labels, 0, 0, i, thatLabels.cols());
					}
				}
			}
			else{
				// Dim is categorical
				// All points == val go in otherMatrix]
				for(int i = 0; i < features.rows(); i++){
					if(features.row(i)[dim] == val){
						thisFeatures.newColumn();
						thisFeatures.newRow();
						thisFeatures.copyPart(features, 0, 0, i, thisFeatures.cols());
						thisLabels.newColumn();
						thisLabels.newRow();
						thisLabels.copyPart(labels, 0, 0, i, thisLabels.cols());
					}
					else{
						thatFeatures.newColumn();
						thatFeatures.newRow();
						thatFeatures.copyPart(features, 0,0, i, thatFeatures.cols());
						thatLabels.newColumn();
						thatLabels.newRow();
						thatLabels.copyPart(labels, 0, 0, i, thatLabels.cols());
					}
				}

			}
			InteriorNode node = new InteriorNode();
			node.a = buildTree(thisFeatures, thisLabels);
			node.b = buildTree(thatFeatures, thatLabels);
			return node;
		}
	}

	void train(Matrix features, Matrix labels)
	{
		root = buildTree(features, labels);	
	}

	void predict(double[] in, double[] out)
	{
		Vec.copy(out, mode);
	}
}