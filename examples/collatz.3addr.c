#include <stdio.h>
#define WriteLine() printf("\n");
#define WriteLong(x) printf(" %lld", x);
#define ReadLong(a) if (fscanf(stdin, "%lld", &a) != 1) a = 0;
#define long long long



// Global Declarations
	long i;
	long j;
	long maxi;
	long k;
	long max;

//Functions
void main()	
{
// Local Variable Declarations
// Function Body
	max = 0;
	i = 5;
	while (max < 270) {
	k = 0;
	j = i;
	while (j != 4) {
	if ((j % 2) == 1) {
	j = (((j + j) + j) + 1) / 2;
	k = k + 2;
	}
else
	{
	j = j / 2;
	k = k + 1;
	}
}
	if (k > max) {
	max = k;
	maxi = i;
	WriteLong((max + 2));
	WriteLong(maxi);
	WriteLine();
	}
	i = i + 1;
	}
	WriteLong((max + 2));
	WriteLong(maxi);
	WriteLine();
}
