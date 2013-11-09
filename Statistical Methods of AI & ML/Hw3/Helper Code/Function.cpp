/*
 * Function.cpp
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */
#include "Function.h"
#include "Util.h"
void Function::multiplyAndMarginalize(vector<Variable*>& marg_variables_,
		vector<Function*>& functions, Function& f, bool to_normalize) {

	vector<Variable*> variables_, variables;
	vector<Variable*> marg_variables;
	for (int i = 0; i < functions.size(); i++) {
		do_set_union(variables_, functions[i]->variables, variables_,
				less_than_comparator_variable);
	}
	// Remove Evidence variables
	for (int i = 0; i < variables_.size(); i++)
		if (!variables_[i]->isEvidence())
			variables.push_back(variables_[i]);
	// Handle the case where the marg_variables are not included in variables
	do_set_intersection(variables, marg_variables_, marg_variables,
			less_than_comparator_variable);
	int num_values = Variable::getDomainSize(variables);
	f = Function();
	f.variables = marg_variables;
	f.table = vector<ldouble>(Variable::getDomainSize(marg_variables), 0.0);
	for (int i = 0; i < num_values; i++) {
		Variable::setAddress(variables, i);
		ldouble value = 1.0;
		for (int j = 0; j < functions.size(); j++) {
			int func_entry = Variable::getAddress(functions[j]->variables);
			value *= functions[j]->table[func_entry];
		}
		f.table[Variable::getAddress(marg_variables)] += value;
	}

	if (to_normalize) {
		ldouble norm_const = 0.0;
		for (int i = 0; i < f.table.size(); i++) {
			norm_const += f.table[i];
		}
		for (int i = 0; i < f.table.size(); i++) {
			f.table[i] /= norm_const;
		}
	}
}
