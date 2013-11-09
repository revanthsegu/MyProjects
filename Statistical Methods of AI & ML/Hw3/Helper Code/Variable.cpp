/*
 * Variable.cpp
 *
 *  Created on: Mar 25, 2013
 *      Author: Vibhav Gogate
 *				The University of Texas at Dallas
 *				All rights reserved
 */
#include "Variable.h"

bool less_than_comparator_variable(const Variable* a, const Variable* b)
{
	return (a->id<b->id);
}



