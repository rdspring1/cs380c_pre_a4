#include <stdio.h>
#define WriteLine() printf("\n");
#define WriteLong(x) printf(" %lld", x);
#define ReadLong(a) if (fscanf(stdin, "%lld", &a) != 1) a = 0;
#define long long long



// Global Declarations
	long a;
	long b;
	long res;

//Functions
void function_1(long a, long b)	
{
// Local Variable Declarations
	long c;
// Function Body
	while (b != 0) {
	c = a;
	a = b;
	b = c % b;
	WriteLong(c);
	WriteLong(a);
	WriteLong(b);
	WriteLine();
	}
	res = a;
}
void main()	
{
// Local Variable Declarations
// Function Body
	a = 25733;
	b = 48611;
	function_1(a, b);
	WriteLong(res);
	WriteLine();
	WriteLine();
	a = 7485671;
	b = 7480189;
	function_1(a, b);
	WriteLong(res);
	WriteLine();
}
