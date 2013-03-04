#include <stdio.h>
#define WriteLine() printf("\n");
#define WriteLong(x) printf(" %lld", x);
#define ReadLong(a) if (fscanf(stdin, "%lld", &a) != 1) a = 0;
#define long long long



// Global Declarations
	struct STRUCT3	{
	long y;
	long x;
	}	a;
	struct STRUCT4	{
	long y;
	long x;
	}	b[3];

//Functions
void main()	
{
// Local Variable Declarations
	long i;
	struct STRUCT1	{
	struct STRUCT2	{
	long r;
	}	z;
	long y;
	long x;
	}	c;
// Function Body
	c.z.r = 987654321;
	a.x = 1;
	a.y = 2;
	c.x = 9;
	c.y = 0;
	b[(0 * 5)].x = 3;
	b[(0 * 5)].y = 4;
	b[(a.x * 5)].x = 5;
	b[(a.x * 5)].y = 6;
	b[((b[((a.x - 1) * 5)].x - 1) * 5)].x = 7;
	b[((b[((a.y - 2) * 5)].x - 1) * 5)].y = 8;
	WriteLong(a.x);
	WriteLong(a.y);
	WriteLine();
	i = 0;
	while (i < 3) {
	WriteLong(b[(i * 5)].x);
	WriteLong(b[(i * 5)].y);
	WriteLine();
	i = i + 1;
	}
	WriteLong(c.x);
	WriteLong(c.y);
	WriteLine();
	WriteLong(c.z.r);
	WriteLine();
}
