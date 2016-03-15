// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;


class NeuralAgent implements IAgent
{
	NeuralNet nn;
	double[] in;

	/// General-purpose constructor
	NeuralAgent(double[] weights) {
		in = new double[20];
		nn = new NeuralNet();
		nn.layers.add(new LayerTanh(in.length, 8));
		nn.layers.add(new LayerTanh(8, 11));
		nn.layers.add(new LayerTanh(11, 2));
		setWeights(weights);
		//nn.init(rand);
		//System.out.println("Weights: " + Integer.toString(countWeights()));
	}


	/// Returns the number of weights necessary to fully-parameterize this agent
	int countWeights() {
		int n = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			n += nn.layers.get(i).countWeights();
		return n;
	}


	/// Sets the parameters of this agent with the specified weights
	void setWeights(double[] weights) {
		if(weights.length != countWeights())
			throw new IllegalArgumentException("Wrong number of weights. Got " + Integer.toString(weights.length) + ", expected " + Integer.toString(countWeights()));
		int start = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			start += nn.layers.get(i).setWeights(weights, start);
	}


	/// Decides what to do
	public double[] update(Controller c, Model m) {
		// Blue position
		in[0] = m.getX() / 600.0 - 0.5;
		in[1] = m.getY() / 600.0 - 0.5;
		in[2] = Math.atan2(in[1], in[0]) / 4.0;
		in[3] = Math.sqrt(in[0] * in[0] + in[1] * in[1]);

		// Blue velocity
		in[4] = m.getXvel() / 100.0;
		in[5] = m.getYvel() / 100.0;
		in[6] = Math.atan2(in[5], in[4]) / 4.0;
		in[7] = Math.sqrt(in[4] * in[4] + in[5] * in[5]);

		// Red position
		in[8] = m.getXOpponent() / 600.0 - 0.5;
		in[9] = m.getYOpponent() / 600.0 - 0.5;
		in[10] = Math.atan2(in[9], in[8]) / 4.0;
		in[11] = Math.sqrt(in[8] * in[8] + in[9] * in[9]);

		// Red velocity
		in[12] = m.getXvelOpponent() / 100.0;
		in[13] = m.getYvelOpponent() / 100.0;
		in[14] = Math.atan2(in[13], in[12]) / 4.0;
		in[15] = Math.sqrt(in[12] * in[12] + in[13] * in[13]);

		// Relative position
		in[16] = in[0] - in[8];
		in[17] = in[1] - in[9];
		in[18] = Math.atan2(in[17], in[16]) / 4.0;
		in[19] = Math.sqrt(in[16] * in[16] + in[17] * in[17]);

		double[] out = nn.forwardProp(in);
		return out;
	}
}
