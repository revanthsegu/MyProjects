/*
 * Function.h
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */

#ifndef FUNCTION_H_
#define FUNCTION_H_

#include <vector>
#include "Variable.h"
#include "Util.h"

using namespace std;

struct Function
{
	int id;
	vector<Variable*> variables;
	vector<ldouble> table;
	Function():id(-1){}
	~Function(){}
	// Multiply the functions given in functions. Marginalize out all variables not mentioned in marg_variables and store the resulting function in function
	// Note that the following method also implements multiplication. If marg_variables equals the union of all variables mentioned in functions
	// then function will store the product of all functions
	static void multiplyAndMarginalize( vector<Variable*>& marg_variables, vector<Function*>& functions, Function& function, bool to_normalize=true);
};
#endif /* FUNCTION_H_ */
