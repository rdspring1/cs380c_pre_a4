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
	long m1[12];
	long m2[12];
	long m3[9];
	long i;
	long j;
	long k;
// Function Body
	i = 0;
	while (i < 4) {
	j = 0;
	while (j < 3) {
	m1[(i * 3) + j] = i + (j * 2);
	WriteLong((i + (j * 2)));
	j = j + 1;
	}
	WriteLine();
	i = i + 1;
	}
	i = 0;
	while (i < 4) {
	j = 0;
	while (j < 3) {
	m2[(j * 4) + i] = m1[(i * 3) + j];
	j = j + 1;
	}
	i = i + 1;
	}
	WriteLine();
	i = 0;
	while (i < 3) {
	j = 0;
	while (j < 4) {
	WriteLong(m2[(i * 4) + j]);
	j = j + 1;
	}
	WriteLine();
	i = i + 1;
	}
	i = 0;
	while (i < 3) {
	j = 0;
	while (j < 3) {
	m3[(i * 3) + j] = 0;
	j = j + 1;
	}
	i = i + 1;
	}
	WriteLine();
	i = 0;
	while (i < 3) {
	j = 0;
	while (j < 3) {
	k = 0;
	while (k < 4) {
	m3[(i * 3) + j] = m3[(i * 3) + j] + (m1[(k * 3) + j] * m2[(i * 4) + k]);
	k = k + 1;
	}
	j = j + 1;
	}
	i = i + 1;
	}
	i = 0;
	while (i < 3) {
	j = 0;
	while (j < 3) {
	WriteLong(m3[(i * 3) + j]);
	j = j + 1;
	}
	WriteLine();
	i = i + 1;
	}
}
