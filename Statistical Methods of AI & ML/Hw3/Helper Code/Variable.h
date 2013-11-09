/*
 * Variable.h
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */

#ifndef VARIABLE_H_
#define VARIABLE_H_

#include <iostream>
#include <vector>
#include <cassert>

using namespace std;

struct Variable {
	// id assigned to the variable
	int id;
	// The number of values in the domain of the variable
	int d;
private:
	// If this variable is not an evidence variable this value is -1. Otherwise it can be assigned a value in the range [0,d-1]
	int value;
	// Use for computing addresses
	int addr_value;
public:
	Variable() :
			id(-1), d(-1), value(-1), addr_value(-1) {
	}
	~Variable() {
	}
	inline bool isEvidence(){return (value!=-1);}
	inline int getValue(){ if (value > -1) return value; else return addr_value;}
	inline void setEvidence(int value_){value=value_;}
	inline void setValue(int value_){ assert(value==-1); addr_value=value_;}
	// Get the maximum domain size of the set of variables
	static int getDomainSize(const vector<Variable*>& variables) {
		int domain_size = 1;
		for (int i = 0; i < variables.size(); i++){
			domain_size *= variables[i]->d;
		}
		return domain_size;
	}

	// Convert assignment to address...Example: The assignment (A1=1,A2=0) is equivalent to the address indexed by integer 2
	inline static int getAddress(const vector<Variable*>& variables) {
		int address = 0;
		int multiplier = 1;
		//for (int i = 0; i < variables.size(); i++) {
		for(int i=variables.size()-1;i>-1;i--){
			address += (multiplier * variables[i]->getValue());
			multiplier *= variables[i]->d;
		}
		return address;
	}

	// Convert address to assignment
	inline static void setAddress(const vector<Variable*>& variables,
			const int address_) {

		int address = address_;
		//for (int i = 0; i < variables.size(); i++) {
		for(int i=variables.size()-1;i>-1;i--){
			if(variables[i]->isEvidence()) continue;
			variables[i]->setValue(address % variables[i]->d);
			address /= variables[i]->d;
		}
	}
};
bool less_than_comparator_variable(const Variable* a, const Variable* b);
#endif /* VARIABLE_H_ */
