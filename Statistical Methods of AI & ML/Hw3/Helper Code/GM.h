/*
 * GM.h
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */

#ifndef GM_H_
#define GM_H_

#include <iostream>
#include <fstream>
#include <set>
using namespace std;
#include "Variable.h"
#include "Function.h"

struct GM
{
	vector<Variable*> variables;
	vector<Function*> functions;
	void readUAIGM(char* uaifilename);
	void readUAIEvidence(char* evidfilename);
	ldouble BE(vector<int>& order);
	void getMinDegreeOrdering(vector<int>& order, vector<set<int> >& clusters);
	void computeWCutset(vector<set<int> >& clusters, int w,vector<int>& wcv);
	void wCutsetSampling(vector<int>& order, vector<int>& wcv, int num_samples);
	void adaptiveWCutsetSampling(vector<int>& order, vector<int>& wcv, int num_samples);
};

#endif /* GM_H_ */
