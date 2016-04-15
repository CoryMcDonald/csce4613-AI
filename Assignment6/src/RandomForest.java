import java.util.ArrayList;

class RandomForest extends SupervisedLearner
{
	Node[] root;
	int numOfTrees;
	Random rand;
	Matrix[] train_feats;
	Matrix[] train_labs;
	private final int MIN_SIZE = 25;
	private final int DEPTH_TIMEOUT = 50;

	RandomForest(){
		this.numOfTrees = 0;
	}

	RandomForest(int numOfTrees){
		this.root = new Node[numOfTrees];
		this.numOfTrees = numOfTrees;
		this.rand = new Random(1234);
		this.train_feats = new Matrix[numOfTrees];
		this.train_labs = new Matrix[numOfTrees];

		for(int i = 0; i < numOfTrees; i++){
			this.train_feats[i] = new Matrix();
			this.train_labs[i] = new Matrix();
		}
	}

	@Override
	String name()
	{
		return "RandomForest";
	}

	Node buildTree(Matrix features, Matrix labels, int depth){
		if(features.rows() != labels.rows())
			throw new IllegalArgumentException("Feature rows doesn't equal label rows.");
		if(features.rows() < MIN_SIZE || depth > DEPTH_TIMEOUT){
			// Return the most common value in column 0 of labels
			LeafNode newLeaf = new LeafNode();
			double[] temp = new double[1];
			temp[0] = labels.mostCommonValue(0);
			newLeaf.label = temp;
			return newLeaf;
		}
		else {
			int dim = rand.nextInt(features.cols());
			double[] row = features.row(rand.nextInt(features.rows()));
			double val = row[dim];
			double value;

			Matrix thisFeatures = new Matrix();
			thisFeatures.copyMetaData(features);
			Matrix thisLabels = new Matrix();
			thisLabels.copyMetaData(labels);
			Matrix thatFeatures = new Matrix();
			thatFeatures.copyMetaData(features);
			Matrix thatLabels = new Matrix();
			thatLabels.copyMetaData(labels);

            if (features.valueCount(dim) == 0) {
                value = features.columnMean(dim);
                for (int i = 0; i < features.rows(); i++) {     
                	double[] fRow = features.row(i);
					double[] lRow = labels.row(i);        
                    if (val < value) {
                        double[] newFeatures = thisFeatures.newRow();
						double[] newLabels = thisLabels.newRow();
						for (int j = 0; j < features.cols(); j++) {
							newFeatures[j] = fRow[j];
						}
						newLabels[0] = lRow[0];
                    } else {
                        double[] newFeatures = thatFeatures.newRow();
						double[] newLabels = thatLabels.newRow();
						for (int j = 0; j < features.cols(); j++) {
							newFeatures[j] = fRow[j];
						}
						newLabels[0] = lRow[0];
                    }
                }
            } 
            else {             
                value = row[rand.nextInt(features.cols())];
                for (int i = 0; i < features.rows(); i++) {    
                	double[] fRow = features.row(i);
					double[] lRow = labels.row(i);       
                    if (value == val) {
                        double[] newFeatures = thisFeatures.newRow();
						double[] newLabels = thisLabels.newRow();
						for (int j = 0; j < features.cols(); j++) {
							newFeatures[j] = fRow[j];
						}
						newLabels[0] = lRow[0];
                    } else {
                        double[] newFeatures = thatFeatures.newRow();
						double[] newLabels = thatLabels.newRow();
						for (int j = 0; j < features.cols(); j++) {
							newFeatures[j] = fRow[j];
						}
						newLabels[0] = lRow[0];
                    }
                }
            }
			InteriorNode node = new InteriorNode(dim, val, features.valueCount(dim));
			node.a = buildTree(thisFeatures, thisLabels, depth++);
			node.b = buildTree(thatFeatures, thatLabels, depth++);

			return node;
		}

	}

	@Override	
	void train(Matrix features, Matrix labels)
	{
		for (int i = 0; i < this.numOfTrees; i++) {
			this.train_feats[i].copyMetaData(features);
			this.train_labs[i].copyMetaData(labels);
			
			for (int j = 0; j < features.rows(); j++) {
				int rowIndex = rand.nextInt(features.rows());
				double[] fRow = features.row(rowIndex);
				double[] lRow = labels.row(rowIndex);
				
				double[] tempRow = this.train_feats[i].newRow();
				double[] tempLabel = this.train_labs[i].newRow();
				
				for (int k = 0; k < features.cols(); k++) {
					tempRow[k] = fRow[k];
				}
				tempLabel[0] = lRow[0];
			}
			root[i] = buildTree(this.train_feats[i], this.train_labs[i], 0);
		}
	}

	void predict(double[] in, double[] out)
	{
		Matrix poll = new Matrix();
		poll.newColumn(100);

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
