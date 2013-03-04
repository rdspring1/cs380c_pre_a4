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
	long temp;
	long array[10];
// Function Body
	i = 0;
	while (i < 10) {
	array[i] = (10 - i) - 1;
	i = i + 1;
	}
	i = 0;
	while (i < 10) {
	WriteLong(array[i]);
	i = i + 1;
	}
	WriteLine();
	i = 0;
	while (i < 10) {
	j = 0;
	while (j < i) {
	if (array[j] > array[i]) {
	temp = array[i];
	array[i] = array[j];
	array[j] = temp;
	}
	j = j + 1;
	}
	i = i + 1;
	}
	i = 0;
	while (i < 10) {
	WriteLong(array[i]);
	i = i + 1;
	}
	WriteLine();
}
