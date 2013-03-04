#include <stdio.h>
#define WriteLine() printf("\n");
#define WriteLong(x) printf(" %lld", x);
#define ReadLong(a) if (fscanf(stdin, "%lld", &a) != 1) a = 0;
#define long long long



// Global Declarations
	long a;
	long m;
	long q;
	long r;
	long count;
	long res;

//Functions
void function_1(long n)	
{
// Local Variable Declarations
// Function Body
	if (n == 0) {
	res = 1;
	}
else
	{
	function_1((n - 1));
	res = n * res;
	}
}
void function_2(long n)	
{
// Local Variable Declarations
	long x;
	long y;
// Function Body
	if (n <= 1) {
	res = 1;
	}
else
	{
	function_2((n - 1));
	x = res;
	function_2((n - 2));
	y = res;
	res = x + y;
	}
}
void function_3(long from, long to)	
{
// Local Variable Declarations
// Function Body
	WriteLong(from);
	WriteLong(to);
	WriteLine();
	count = count + 1;
}
void function_4(long from, long by, long to, long height)	
{
// Local Variable Declarations
// Function Body
	if (height == 1) {
	function_3(from, to);
	}
else
	{
	function_4(from, to, by, (height - 1));
	function_3(from, to);
	function_4(by, from, to, (height - 1));
	}
}
void function_5(long height)	
{
// Local Variable Declarations
// Function Body
	count = 0;
	function_4(1, 2, 3, height);
	WriteLine();
	WriteLong(count);
	WriteLine();
}
void main()	
{
// Local Variable Declarations
// Function Body
	a = 16807;
	m = 127;
	m = (m * 256) + 255;
	m = (m * 256) + 255;
	m = (m * 256) + 255;
	q = m / a;
	r = m % a;
	function_1(7);
	WriteLong(res);
	WriteLine();
	WriteLine();
	function_2(11);
	WriteLong(res);
	WriteLine();
	WriteLine();
	function_5(3);
	WriteLine();
}
