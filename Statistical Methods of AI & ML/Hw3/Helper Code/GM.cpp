/*
 * GM.cpp
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */
#include "GM.h"
#include <fstream>
#include <cstdlib>
#include <set>
using namespace std;
void GM::readUAIGM(char* uaifilename) {
	ifstream infile(uaifilename);
	if(!infile.good()){
		cerr<<"Error opening GM file\n";
		exit(-1);
	}
	int num_variables;
	string tmp_string;
	infile >> tmp_string;
	if (tmp_string.compare("BAYES") == 0) {
		infile >> num_variables;
		// Read domains
		variables = vector<Variable*>(num_variables);
		for (int i = 0; i < num_variables; i++) {
			variables[i] = new Variable();
			variables[i]->id=i;
			infile>>variables[i]->d;
		}
		int num_functions;
		infile >> num_functions;
		vector<vector<Variable*> > parents(num_functions);
		vector<int> func_order(num_functions);
		for (int i = 0; i < num_functions; i++) {
			// Read parents of variables
			int num_parents;
			infile >> num_parents;
			num_parents--;
			vector<Variable*> curr_parents(num_parents);
			for (int j = 0; j < num_parents; j++) {
				int temp;
				infile >> temp;
				curr_parents[j] = variables[temp];
			}
			int var;
			infile >> var;
			parents[var] = curr_parents;
			func_order[i]=var;
		}
		functions = vector<Function*>(num_functions);
		for (int i = 0; i < num_functions; i++) {

			int var=func_order[i];
			int num_probabilities;
			infile>>num_probabilities;
			functions[var] = new Function();
			functions[var]->id=i;
			vector<Variable*> unsorted_variables;
			functions[var]->variables=parents[var];
			functions[var]->variables.push_back(variables[var]);
			unsorted_variables=functions[var]->variables;
			sort(functions[var]->variables.begin(), functions[var]->variables.end(),less_than_comparator_variable);
			functions[var]->table = vector < ldouble > (num_probabilities);
			for (int j = 0; j < num_probabilities; j++) {
				Variable::setAddress(unsorted_variables, j);
				infile>>functions[var]->table[Variable::getAddress(functions[var]->variables)];
			}
		}
	} else  {
		infile >> num_variables;
		// Read domains
		variables = vector<Variable*>(num_variables);
		for (int i = 0; i < num_variables; i++) {
			variables[i] = new Variable();
			variables[i]->id = i;
			infile >> variables[i]->d;
		}
		int num_functions;
		infile >> num_functions;
		vector < vector<Variable*> > scope(num_functions);
		for (int i = 0; i < num_functions; i++) {
			// Read parents of variables
			int num_vars_in_func;
			infile >> num_vars_in_func;
			scope[i] = vector<Variable*>(num_vars_in_func);
			for (int j = 0; j < num_vars_in_func; j++) {
				int temp;
				infile >> temp;
				scope[i][j] = variables[temp];
			}
		}
		functions = vector<Function*>(num_functions);
		for (int i = 0; i < num_functions; i++) {
			int num_probabilities;
			infile >> num_probabilities;
			functions[i] = new Function();
			functions[i]->id=i;
			functions[i]->variables=scope[i];
			sort(functions[i]->variables.begin(), functions[i]->variables.end(),less_than_comparator_variable);
			functions[i]->table = vector < ldouble > (num_probabilities);
			for (int j = 0; j < num_probabilities; j++) {
				Variable::setAddress(scope[i], j);
				infile >> functions[i]->table[Variable::getAddress(functions[i]->variables)];
			}
		}
	}
	infile.close();
}

void GM::readUAIEvidence(char* evidfilename)
{
	ifstream infile(evidfilename);
	if (!infile.good()) {
		infile.close();
		return;
	}
	int num_evidence;
	infile>>num_evidence;
	for(int i=0;i<num_evidence;i++){
		int var,value;
		infile>>var;infile>>value;
		variables[var]->setEvidence(value);
	}
	infile.close();
}

ldouble GM::BE(vector<int> &order)
{
	ldouble pe=1;
	vector<vector<Function*> > buckets (order.size());

	vector<int> var_in_pos(variables.size());
	for(int i=0;i<order.size();i++)
		var_in_pos[order[i]]=i;

	// First put the functions in the proper buckets
	for(int i=0;i<functions.size();i++)
	{
		int pos=variables.size();
		// Boundary case
		// If all variables in a function are evidence variables, simply multiple the entry with pe
		Function* function=functions[i];
		bool all_assigned=true;
		for(int j=0;j<function->variables.size();j++)
		{
			if(function->variables[j]->isEvidence()) continue;
			if(var_in_pos[function->variables[j]->id] < pos)
				pos=var_in_pos[function->variables[j]->id];
			all_assigned=false;
		}
		if(all_assigned) {
			pe*=function->table[Variable::getAddress(function->variables)];
		}
		else{
			assert(pos!=(int)order.size());
			buckets[pos].push_back(function);
		}
	}

	//cout<<"Now processing buckets\n";
	//Process buckets
	for(int i=0;i<buckets.size();i++)
	{
		if(buckets[i].empty())
			continue;

		vector<Variable*> bucket_variables_;
		for(int j=0;j<buckets[i].size();j++)
		{
			do_set_union(bucket_variables_,buckets[i][j]->variables,bucket_variables_,less_than_comparator_variable);
		}
		vector<Variable*> bucket_variables;
		for(int j=0;j<bucket_variables_.size();j++)
			if(!bucket_variables_[j]->isEvidence()) bucket_variables.push_back(bucket_variables_[j]);
		vector<Variable*> bucket_variable;
		bucket_variable.push_back(variables[order[i]]);
		vector<Variable*> marg_variables;
		do_set_difference(bucket_variables,bucket_variable,marg_variables,less_than_comparator_variable);

		Function* function= new Function();

		Function::multiplyAndMarginalize(marg_variables,buckets[i],*function,false);
		if(function->variables.empty())
		{
			assert((int)function->table.size()==1);
			pe*=function->table[0];
			if (function->id <0){delete(function);}
			continue;
		}
		//Put the function in the appropriate bucket
		int pos=order.size();
		for(int j=0;j<function->variables.size();j++)
		{
			if(var_in_pos[function->variables[j]->id] < pos)
				pos=var_in_pos[function->variables[j]->id];
		}
		assert(pos!=(int)order.size());
		assert(pos > i);
		buckets[pos].push_back(function);
		for(int j=0;j<buckets[i].size();j++)
		{
			if (buckets[i][j]!=NULL && buckets[i][j]->id <0){
				delete(buckets[i][j]);
			}
		}
		buckets[i].clear();
	}
	for(int i=0;i<buckets.size();i++){
		for(int j=0;j<buckets[i].size();j++){
			if (buckets[i][j]!=NULL && buckets[i][j]->id <0){
				delete(buckets[i][j]);
			}
		}
	}
	buckets.clear();
	return pe;
}


void GM::getMinDegreeOrdering(vector<int>& order, vector<set<int> >& clusters)
{

	// number of non-evidence variables
	int nne=0;
	vector<bool> processed(variables.size());
	for(int i=0;i<variables.size();i++){
		if(variables[i]->isEvidence()) {
			processed[i]=true;
		}
		else{
			nne++;
		}
	}

	order=vector<int>(nne);
	clusters=vector<set<int> >(nne);
	vector<set<int> > graph(variables.size());

	for(int i=0;i<functions.size();i++)
	{
		// Ignore the evidence variables
		for(int j=0;j<functions[i]->variables.size();j++)
		{
			int a=functions[i]->variables[j]->id;
			if(variables[a]->isEvidence()) continue;
			for(int k=j+1;k<functions[i]->variables.size();k++)
			{
				int b=functions[i]->variables[k]->id;
				if(variables[b]->isEvidence()) continue;
				graph[a].insert(b);
				graph[b].insert(a);
			}
		}
	}
	int max_cluster_size=0;
	for(int i=0;i<nne;i++)
	{
		//Find the node with the minimum number of nodes
		int min=variables.size();
		for(int j=0;j<graph.size();j++){
			if (processed[j]) continue;
			if (min > graph[j].size()){
				order[i]=j;
				min=graph[j].size();
			}
		}
		// Connect the neighbors of order[i] to each other
		int var=order[i];
		processed[var]=true;
		for(set<int>::iterator a=graph[var].begin();a!=graph[var].end();a++){
			for(set<int>::iterator b=graph[var].begin();b!=graph[var].end();b++){
				if (*a==*b) continue;
				graph[*a].insert(*b);
				graph[*b].insert(*a);
			}
		}
		clusters[i]=graph[var];
		if(clusters[i].size()>max_cluster_size) max_cluster_size=clusters[i].size();
		// Remove var from the graph
		for(set<int>::iterator a=graph[var].begin();a!=graph[var].end();a++){
			graph[*a].erase(var);
		}
		graph[var].clear();
	}
	cout<<"Max cluster size ="<<max_cluster_size<<endl;
}
