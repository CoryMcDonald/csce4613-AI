import java.util.ArrayList;

class RandomForest extends SupervisedLearner
{
	Node[] root;
	int numOfTrees;
	Random rand;
	Matrix[] train_feats, train_labs;
	int minSize;

	RandomForest(){
		this.numOfTrees = 0;
		rand = new Random(1234);
	}

	RandomForest(int numOfTrees){
		this.numOfTrees = numOfTrees;
		rand = new Random(1234);
		train_feats = new Matrix[numOfTrees];
		train_labs = new Matrix[numOfTrees];
		root = new Node[numOfTrees];

		for(int i = 0; i < numOfTrees; i++){
			train_feats[i] = new Matrix();
			train_labs[i] = new Matrix();
		}
	}

	String name()
	{
		return "RandomForest";
	}

	Node buildTree(Matrix features, Matrix labels){
		if(features.rows() != labels.rows())
			throw new IllegalArgumentException("Feature rows doesn't equal label rows.");

		int dim = rand.nextInt(features.cols());
		double val;

		Matrix thisFeatures = new Matrix();
		thisFeatures.copyMetaData(features);
		Matrix thisLabels = new Matrix();
		thisLabels.copyMetaData(labels);
		Matrix thatFeatures = new Matrix();
		thatFeatures.copyMetaData(features);
		Matrix thatLabels = new Matrix();
		thatLabels.copyMetaData(labels);

		if(features.rows() < minSize){
			LeafNode node = new LeafNode(features);
			double[] temp = new double[1];
			temp[0] = labels.mostCommonValue(0);
			node.label = temp;
			return node;
		}
		else {
            if (features.valueCount(dim) == 0) {
                val = features.columnMean(dim);
                for (int i = 0; i < features.rows(); i++) {
                    double[] row = features.row(i);
                    double[] label = labels.row(i);
                    double value = row[dim];
                    
                    if (value < val) {
                        double[] newFeatures = thisFeatures.newRow();
                        double[] newLabels = thisLabels.newRow();
                        for (int j = 0; j < features.cols(); j++) {
                            newFeatures[j] = row[j];
                        }
                        newLabels[0] = label[0];
                    } else {
                        double[] newFeatures = thatFeatures.newRow();
                        double[] newLabels = thatLabels.newRow();
                        for (int j = 0; j < features.cols(); j++) {
                            newFeatures[j] = row[j];
                        }
                        newLabels[0] = label[0];
                    }
                }
            } else {
                double[] temp = features.row(rand.nextInt(features.rows()));
                val = temp[rand.nextInt(features.cols())];
                
                for (int i = 0; i < features.rows(); i++) {
                    double[] row = features.row(i);
                    double[] label = labels.row(i);
                    double value = row[dim];
                    
                    if (value == val) {
                        double[] newFeatures = thisFeatures.newRow();
                        double[] newLabels = thisLabels.newRow();
                        for (int j = 0; j < features.cols(); j++) {
                            newFeatures[j] = row[j];
                        }
                        newLabels[0] = label[0];
                    } else {
                        double[] newFeatures = thatFeatures.newRow();
                        double[] newLabels = thatLabels.newRow();
                        for (int j = 0; j < features.cols(); j++) {
                            newFeatures[j] = row[j];
                        }
                        newLabels[0] = label[0];
                    }
                }
            }
			InteriorNode node = new InteriorNode(dim, val, features.valueCount(dim));
			node.a = buildTree(thisFeatures, thisLabels);
			node.b = buildTree(thatFeatures, thatLabels);

			node.attribute = dim;
			node.pivot = val;

			return node;
		}

	}

	// Give each DecisionTree an equal vote in predictions.
	// For categorical labels, it predicts the value that is most common among the predictions of the models it aggregates. 
	// For continuous labels, it predicts the mean of the predictions of the models it aggregates.
	void train(Matrix features, Matrix labels)
	{
		for(int i = 0; i < numOfTrees; i++){
			this.train_feats[i].copyMetaData(features);
			this.train_labs[i].copyMetaData(labels);

			for(int j = 0; j < features.rows(); j++){
				// Randomly choose row
				double[] row = features.row(rand.nextInt(features.rows()));
				double[] label = labels.row(rand.nextInt(labels.rows()));

				double[] tempRow = this.train_feats[i].newRow();
				double[] tempLabel = this.train_labs[i].newRow();

				for(int k = 0; k < features.cols(); k++){
					tempRow[k] = row[k];
				}

				tempLabel[0] = label[0];
			}
			root[i] = buildTree(this.train_feats[i], this.train_labs[i]);
		}
	}

	void predict(double[] in, double[] out)
	{
		Matrix poll = new Matrix();
		poll.newColumn();

		for(int i = 0; i < numOfTrees; i++){
			Node n = root[i];

			while(!n.isLeaf()){
				if(((InteriorNode)n).isCont == 0){
					if(in[((InteriorNode)n).attribute] < ((InteriorNode)n).pivot)
						n = ((InteriorNode)n).a;
					else
						n = ((InteriorNode)n).b;
				}
				else{
					if(in[((InteriorNode)n).attribute] == ((InteriorNode)n).pivot)
						n = ((InteriorNode)n).a;
					else
						n = ((InteriorNode)n).b;
				}
			}
			double[] temp = poll.newRow();
			temp[0] = ((LeafNode)n).label[0];
		}
		out[0] = poll.mostCommonValue(0);
	}
}
