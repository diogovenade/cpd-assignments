#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

 
void OnMult(int m_ar, int m_br) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);
	
	for (i = 0; i < m_ar; i++) {
		for (j = 0; j < m_ar; j++) {
			phc[i * m_ar + j] = 0.0;
		}
	}



    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
    SYSTEMTIME Time1, Time2; // variables to store the time

	char st[100]; // string to store the time
	int i, j, k; // loop variables

	double *pha, *phb, *phc; // pointers to the matrices
	


	//allocating memory for the matrices (later to be "freed")
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double)); 
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));


	// Initialize the matrices (a and b respectively)
	// pha is a matrix of 1's
	// phb is a matrix of 1,2,3,4,5,6,7,8,9,10
    for (i=0; i<m_ar; i++){
		for (int j=0; j<m_ar; j++){
			pha[i*m_ar + j] = (double)1.0;
		}
	}

	for (i = 0; i < m_br; i++)
	{
        for (j = 0; j < m_br; j++){
            phb[i * m_br + j] = i + 1;
		}
	}

	for (i = 0; i < m_ar; i++) {
        for (j = 0; j < m_ar; j++) {
            phc[i * m_ar + j] = 0.0;
        }
    }


	double temp;
	Time1 = clock(); //Initiate the clock, starting the performance measure

	//	Matrix multiplication
	//		- Iterate through the rows of the matrix A
	//		- Iterate through the columns of the matrix B
	//		- Iterate through the rows of the matrix B
	//		- Multiply the value of the element at index (i, k) in the matrix A by the value of the element at index (k, j) 
	//	  	  in the matrix B and add it to the element at index (i, j) in the matrix C
	//	This is done for all elements in the matrix A and B
	//	The result is stored in the matrix C

	for(i=0; i<m_ar; i++){
		for (k=0; k<m_ar; k++){
			temp = pha[i * m_ar + k];
			for (j=0; j<m_br; j++){
				phc[i*m_ar+j] += temp * phb[k*m_br+j];
			}
		}
	}

	Time2 = clock(); //Stop the clock, ending the performance measure


	//Time to perform the operation calculation and display
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st; 


	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;


	//Free matrices space
    free(pha);
    free(phb);
    free(phc);


}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k, l, m, n; // loop variables

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double)); 
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for (i=0; i<m_ar; i++){
		for (int j=0; j<m_ar; j++){
			pha[i*m_ar + j] = (double)1.0;
		}
	}

	for (i = 0; i < m_br; i++)
	{
        for (j = 0; j < m_br; j++){
            phb[i * m_br + j] = i + 1;
		}
	}

	for (i = 0; i < m_ar; i++) {
        for (j = 0; j < m_ar; j++) {
            phc[i * m_ar + j] = 0.0;
        }
    }
    
    Time1 = clock();

    for (i=0; i<m_ar; i+=bkSize) {
        for (j=0; j<m_br; j+=bkSize) {
            for (k=0; k<m_ar; k+=bkSize) {

                for (l=i; l<min(i+bkSize, m_ar); l++) {
                    for (m=k; m<min(k+bkSize, m_ar); m++) {
                    	temp = pha[l*m_ar+m];
                        for (n=j; n<min(j+bkSize, m_br); n++) {
                            phc[l*m_ar+n] += temp * phb[m*m_br+n];
                        }
                    }
                }
            }
        }
    }

    Time2 = clock();

    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    // display 10 elements of result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for (i = 0; i < 1; i++) {
        for (j = 0; j < min(10, m_br); j++) {
            cout << phc[j] << " ";
        }
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

double OnMultLineParA(int m_ar, int m_br)
{

	int ret;
	ret = PAPI_library_init(PAPI_VER_CURRENT);

	if (ret != PAPI_VER_CURRENT)
		std::cout << "FAIL" << endl;

    double Time1, Time2;

	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;
	double temp;
	


	pha = (double *)malloc((m_ar * m_ar) * sizeof(double)); 
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for (i=0; i<m_ar; i++){
		for (int j=0; j<m_ar; j++){
			pha[i*m_ar + j] = (double)1.0;
		}
	}

	for (i = 0; i < m_br; i++)
	{
        for (j = 0; j < m_br; j++){
            phb[i * m_br + j] = i + 1;
		}
	}

	for (i = 0; i < m_ar; i++) {
        for (j = 0; j < m_ar; j++) {
            phc[i * m_ar + j] = 0.0;
        }
    }


	Time1 = omp_get_wtime();

	# pragma omp parallel for
	for(i=0; i<m_ar; i++){
		for (k=0; k<m_ar; k++){
			temp = pha[i * m_ar + k];
			for (j=0; j<m_br; j++){
				phc[i*m_ar+j] += temp * phb[k*m_br+j];
			}
		}
	}

	Time2 = omp_get_wtime();

	double elapsedTime = Time2 - Time1;
	sprintf(st, "Time: %3.3f seconds\n", elapsedTime);
	cout << st;


	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;


    free(pha);
    free(phb);
    free(phc);

	return elapsedTime;


}

double OnMultLineParB(int m_ar, int m_br)
{
    double Time1, Time2;

	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;
	


	pha = (double *)malloc((m_ar * m_ar) * sizeof(double)); 
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for (i=0; i<m_ar; i++){
		for (int j=0; j<m_ar; j++){
			pha[i*m_ar + j] = (double)1.0;
		}
	}

	for (i = 0; i < m_br; i++)
	{
        for (j = 0; j < m_br; j++){
            phb[i * m_br + j] = i + 1;
		}
	}

	for (i = 0; i < m_ar; i++) {
        for (j = 0; j < m_ar; j++) {
            phc[i * m_ar + j] = 0.0;
        }
    }


	Time1 = omp_get_wtime();

	double temp;

	#pragma omp parallel private(i, k, temp)
	for(i=0; i<m_ar; i++){
		for (k=0; k<m_ar; k++){
			temp = pha[i * m_ar + k];
			#pragma omp for
			for (j=0; j<m_br; j++){
				phc[i*m_ar+j] += temp * phb[k*m_br+j];
			}
		}
	}

	Time2 = omp_get_wtime();

	double elapsedTime = Time2 - Time1;

	sprintf(st, "Time: %3.3f seconds\n", elapsedTime);
	cout << st;


	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;


    free(pha);
    free(phb);
    free(phc);

	return elapsedTime;


}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{
	

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[3];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

	ret = PAPI_add_event(EventSet, PAPI_DP_OPS);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_DP_OPS" << endl;


	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "4. Line Multiplication (Parallel A)" << endl;
		cout << "5. Line Multiplication (Parallel B)" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;
   		col = lin;


		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		double elapsedTime;

		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(lin, col);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				OnMultBlock(lin, col, blockSize);  
				break;
			case 4:
				elapsedTime = OnMultLineParA(lin, col);  
				break;
			case 5:
				elapsedTime = OnMultLineParB(lin, col);  
				break;

		}

  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
  		printf("L2 DCM: %lld \n",values[1]);

		if (op == 4 || op == 5) {
			printf("MFLOPS: %f \n",(double)values[2]/(elapsedTime*1e6));
		}

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 



	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_DP_OPS );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
	

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}