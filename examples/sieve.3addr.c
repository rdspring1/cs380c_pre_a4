#include <stdio.h>
#define WriteLine() printf("\n");
#define WriteLong(x) printf(" %lld", x);
#define ReadLong(a) if (fscanf(stdin, "%lld", &a) != 1) a = 0;
#define long long long



// Global Declarations

//Functions
void main()	
{
// Local Variable Declarations
	long i;
	long j;
	long is_prime[1000];
// Function Body
	is_prime[0] = 0;
	is_prime[1] = 0;
	i = 2;
	while (i < 1000) {
	is_prime[i] = 1;
	i = i + 1;
	}
	i = 2;
	while (i < 1000) {
	if (is_prime[i] != 0) {
	j = 2;
	while ((i * j) < 1000) {
	is_prime[(i * j)] = 0;
	j = j + 1;
	}
}
	i = i + 1;
	}
	i = 2;
	while (i < 1000) {
	if (is_prime[i] != 0) {
	WriteLong(i);
	}
	i = i + 1;
	}
	WriteLine();
}
