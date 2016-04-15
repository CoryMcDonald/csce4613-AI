import java.util.Random;

public class RandomForest extends SupervisedLearner { 
    private final int MIN_SIZE = 15;

    Node[] root;
    int numOfTrees;
    Random rand;
    Matrix[] train_feats;
    Matrix[] train_labs;
    
    public RandomForest(int numOfTrees) {
        this.numOfTrees = numOfTrees;
        this.root = new Node[numOfTrees];
        this.rand = new Random(1456);
        
        this.train_feats = new Matrix[numOfTrees];
        this.train_labs = new Matrix[numOfTrees];
        
        
        for (int i = 0; i < numOfTrees; i++) {
            this.train_feats[i] = new Matrix();
            this.train_labs[i] = new Matrix();
        }
    }
    
    @Override
    String name() 
    {
        return "RandomForest";
    }
    
    private Node buildTree(Matrix features, Matrix labels){
        if(features.rows() != labels.rows())
            throw new IllegalArgumentException("Feature rows doesn't equal label rows.");       
        if(features.rows() < MIN_SIZE){
            LeafNode nLeaf = new LeafNode();
            double[] temp = new double[1];
            temp[0] = labels.mostCommonValue(0);
            nLeaf.label = temp;
            return nLeaf; 
        }else{
            int dim = rand.nextInt(features.cols());
            double[] row = features.row(rand.nextInt(features.rows()));
            double val;
            
            Matrix thisFeatures = new Matrix();
            thisFeatures.copyMetaData(features);
            Matrix thisLabels = new Matrix();
            thisLabels.copyMetaData(labels);
            Matrix thatFeatures = new Matrix();
            thatFeatures.copyMetaData(features);
            Matrix thatLabels = new Matrix();
            thatLabels.copyMetaData(labels);

            if(features.valueCount(dim) == 0){
                val = features.columnMean(dim);
                
                for(int i = 0; i < features.rows(); i++){
                    double[] fRow = features.row(i);
                    double[] lRow = labels.row(i);
                    double value = fRow[dim];
                    
                    if (value < val){
                        double[] newFeatures = thisFeatures.newRow();
                        double[] newLabels = thisLabels.newRow();
                        
                        for(int j = 0; j < features.cols(); j++)
                            newFeatures[j] = fRow[j];
                        
                        newLabels[0] = lRow[0];
                    }
                    else{
                        double[] newFeatures = thatFeatures.newRow();
                        double[] newLabels = thatLabels.newRow();
                        
                        for(int j = 0; j < features.cols(); j++)
                            newFeatures[j] = fRow[j];
                        
                        newLabels[0] = lRow[0];
                    }
                }
            }else{
                val = row[rand.nextInt(features.cols())];
                
                for(int i = 0; i < features.rows(); i++){
                    double[] fRow = features.row(i);
                    double[] lRow = labels.row(i);
                    double value = fRow[dim];
                    
                    if(value == val){
                        double[] newFeatures = thisFeatures.newRow();
                        double[] newLabels = thisLabels.newRow();
                        
                        for(int j = 0; j < features.cols(); j++)
                            newFeatures[j] = fRow[j];
                        
                        newLabels[0] = lRow[0];
                    }else{
                        double[] newFeatures = thatFeatures.newRow();
                        double[] newLabels = thatLabels.newRow();
                        
                        for(int j = 0; j < features.cols(); j++)
                            newFeatures[j] = fRow[j];
                        
                        newLabels[0] = lRow[0];
                    }
                }
            }
            
            InteriorNode node = new InteriorNode(dim, val, features.valueCount(dim));
            node.a = buildTree(thisFeatures, thisLabels);
            node.b = buildTree(thatFeatures, thatLabels);

            return node;
        }
    }
    
    @Override
    void train(Matrix features, Matrix labels){
        for(int i = 0; i < numOfTrees; i++){
            this.train_feats[i].copyMetaData(features);
            this.train_labs[i].copyMetaData(labels);
            
            for(int j = 0; j < features.rows(); j++){
                int r = rand.nextInt(features.rows());
                
                double[] fRow = features.row(r);
                double[] lRow = labels.row(r);
                
                double[] tempRow = this.train_feats[i].newRow();
                double[] tempLabel = this.train_labs[i].newRow();
                
                for(int k = 0; k < features.cols(); k++)
                    tempRow[k] = fRow[k];
                
                tempLabel[0] = lRow[0];
            }
            
            root[i] = buildTree(this.train_feats[i], this.train_labs[i]);
        }
    }
    
    @Override
    void predict(double[] in, double[] out){
        Matrix poll  = new Matrix();
        poll.newColumn();
        
        for(int i = 0; i < numOfTrees; i++){
            Node n = root[i];
            
            while (!n.isLeaf()){
                double val = in[((InteriorNode) n).attribute];
                
                if(((InteriorNode) n).isCont == 0){
                    if(val < ((InteriorNode) n).pivot)
                        n = ((InteriorNode)n).a;
                    else
                        n = ((InteriorNode)n).b;
                }
                else{
                    if(val == ((InteriorNode) n).pivot)
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