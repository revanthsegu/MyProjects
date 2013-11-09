/*
 * Main.cpp
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */




#include "GM.h"

int main(int argc, char* argv[])
{
	if (argc < 2){
		cerr<<"Usage: "<<argv[0]<<" <uaifile> <evidfile>\n";
		exit(-1);
	}
	GM gm;
	gm.readUAIGM(argv[1]);
	gm.readUAIEvidence(argv[2]);

	vector<int> order;
	vector<set<int> > clusters;
	gm.getMinDegreeOrdering(order,clusters);

	ldouble pe=gm.BE(order);
	cout<<"The partition function or P(e) is "<<pe<<endl;

	/*
	int num_samples=atoi(argv[3]);
	int w=atoi(argv[4]);
	vector<int> wcv;
	gm.computeWCutset(clusters, w,wcv);
	gm.wCutsetSampling(order,  wcv, num_samples);
	*/
}
