
package com.alpine.datamining.tools.matrix;

import org.apache.log4j.Logger;

   

public class LUDecomposition implements java.io.Serializable {
       private static final Logger itsLogger = Logger.getLogger(LUDecomposition.class);
	
	private static final long serialVersionUID = 2828562063585877819L;
	private Matrix A;

   private double[][] LU;

   
   private int m, n, pivsign; 

   
   private int[] piv;
   
   private boolean singular = false;
   
   private boolean []zeroDiag;

   public boolean isSingular() {
	return singular;
}

public void setSingular(boolean singular) {
	this.singular = singular;
}

public boolean[] getZeroDiag() {
	return zeroDiag;
}

public void setZeroDiag(boolean[] zeroDiag) {
	this.zeroDiag = zeroDiag;
}





   public LUDecomposition (Matrix A) {

   // Use a "left-looking", dot-product, Crout/Doolittle algorithm.
	   
	   this.A = (Matrix)A.clone();
      LU = A.getArrayCopy();
      m = A.getRowDimension();
      n = A.getColumnDimension();
      piv = new int[m];
      for (int i = 0; i < m; i++) {
         piv[i] = i;
      }
      pivsign = 1;
      double[] LUrowi;
      double[] LUcolj = new double[m];

      // Outer loop.

      for (int j = 0; j < n; j++) {

         // Make a copy of the j-th column to localize references.

         for (int i = 0; i < m; i++) {
            LUcolj[i] = LU[i][j];
         }

         // Apply previous transformations.

         for (int i = 0; i < m; i++) {
            LUrowi = LU[i];

            // Most of the time is spent in the following dot product.

            int kmax = Math.min(i,j);
            double s = 0.0;
            for (int k = 0; k < kmax; k++) {
               s += LUrowi[k]*LUcolj[k];
            }

            LUrowi[j] = LUcolj[i] -= s;
         }
   
         // Find pivot and exchange if necessary.

         int p = j;
         for (int i = j+1; i < m; i++) {
            if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
               p = i;
            }
         }
         if (p != j) {
            for (int k = 0; k < n; k++) {
               double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
            }
            int k = piv[p]; piv[p] = piv[j]; piv[j] = k;
            pivsign = -pivsign;
         }

         // Compute multipliers.
         
         if (j < m & LU[j][j] != 0.0) {
            for (int i = j+1; i < m; i++) {
               LU[i][j] /= LU[j][j];
            }
         }
      }
   }





   

   public boolean isNonsingular () {
      for (int j = 0; j < n; j++) {
         if (LU[j][j] == 0)
            return false;
      }
      return true;
   }

   

   public Matrix getL () {
      Matrix X = new Matrix(m,n);
      double[][] L = X.getArray();
      for (int i = 0; i < m; i++) {
         for (int j = 0; j < n; j++) {
            if (i > j) {
               L[i][j] = LU[i][j];
            } else if (i == j) {
               L[i][j] = 1.0;
            } else {
               L[i][j] = 0.0;
            }
         }
      }
      return X;
   }

   

   public Matrix getU () {
      Matrix X = new Matrix(n,n);
      double[][] U = X.getArray();
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            if (i <= j) {
               U[i][j] = LU[i][j];
            } else {
               U[i][j] = 0.0;
            }
         }
      }
      return X;
   }

   

   public int[] getPivot () {
      int[] p = new int[m];
      for (int i = 0; i < m; i++) {
         p[i] = piv[i];
      }
      return p;
   }

   

   public double[] getDoublePivot () {
      double[] vals = new double[m];
      for (int i = 0; i < m; i++) {
         vals[i] = (double) piv[i];
      }
      return vals;
   }

   

   public double det () {
      if (m != n) {
         throw new IllegalArgumentException("Matrix must be square.");
      }
      double d = (double) pivsign;
      for (int j = 0; j < n; j++) {
         d *= LU[j][j];
      }
      return d;
   }

   

   public Matrix solve1(Matrix B) {
	   if ( m == 0 || n == 0)
	   {
	         throw new IllegalArgumentException("Matrix dimension is zero");
	   }
	  StringBuffer diag = new StringBuffer();
	  StringBuffer pivot = new StringBuffer();
	  StringBuffer zero = new StringBuffer();
	  zeroDiag = new boolean[Math.max(m, n)];
	  int zeroDiagCount = 0;
	  for(int i = 0; i < m && i < n ; i++)
	  {
		  diag.append(","+LU[i][i]);
		  pivot.append(","+this.piv[i]);
		  if (LU[i][i] == 0)
		  {
			  zero.append(",true");
			  zeroDiag[i] = true;
			  singular = true;
			  zeroDiagCount++;
		  }
		  else
		  {
			  zero.append(",false");
			  zeroDiag[i] = false;
		  }
	  }
	  itsLogger.debug("diag"+diag.toString());
	  itsLogger.debug("pivot"+pivot.toString());
	  itsLogger.debug("zeroz"+zero.toString());
      if (B.getRowDimension() != m) {
         throw new IllegalArgumentException("Matrix row dimensions must agree.");
      }
//      if (!this.isNonsingular()) {
//         throw new RuntimeException("Matrix is singular.");
//      }
      if (singular)
      {
    	  Matrix dropZeroDiag = new Matrix(m-zeroDiagCount,n - zeroDiagCount);
    	  Matrix dropZeroB = new Matrix(m-zeroDiagCount,n - zeroDiagCount);
    	  int newI = 0;
    	  for(int i = 0; i < m; i++)
    	  {
			  int newJ = 0;
    		  if(zeroDiag[i])
    		  {
    			  continue;
    		  }
    		  for (int j = 0; j < n; j++)
    		  {
    			  if (zeroDiag[j])
    			  {
    				  continue;
    			  }
    			  dropZeroDiag.set(newI, newJ, A.get(i,j));
    			  dropZeroB.set(newI, newJ, B.get(i, j));
    			  newJ++;
    		  }
    		  newI++;
    	  }
    	  LUDecomposition dropZeroLUD = new LUDecomposition(dropZeroDiag);
    	  Matrix ret = dropZeroLUD.solve(dropZeroB);
    	  if (dropZeroLUD.isSingular())
    	  {
    		  newI = 0;
    		  for ( int i = 0; i < zeroDiag.length; i++)
    		  {
				  if (zeroDiag[i])
				  {
					  continue;
				  }
    			  for(int j = 0; j < dropZeroLUD.getZeroDiag().length; j++)
    			  {
    				  zeroDiag[i] = dropZeroLUD.getZeroDiag()[newI];
    			  }
				  newI++;
    		  }
    	  }
    	  return ret;
      }
      else
      {

      // Copy right hand side with pivoting
      int nx = B.getColumnDimension();
      Matrix Xmat = B.getMatrix(piv,0,nx-1);
      double[][] X = Xmat.getArray();

      // Solve L*Y = B(piv,:)
      for (int k = 0; k < n; k++) {
         for (int i = k+1; i < n; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      // Solve U*X = Y;
      for (int k = n-1; k >= 0; k--) {
         for (int j = 0; j < nx; j++) {
            X[k][j] /= LU[k][k];
         }
         for (int i = 0; i < k; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      return Xmat;
      }
   }
   
   
   public Matrix solve (Matrix B) {
	   if ( m == 0 || n == 0)
	   {
	         throw new IllegalArgumentException("Matrix dimension is zero");
	   }
//	  StringBuffer diag = new StringBuffer();
//	  StringBuffer pivot = new StringBuffer();
//	  StringBuffer zero = new StringBuffer();
	  zeroDiag = new boolean[Math.max(m, n)];
	  int zeroDiagCount = 0;
	  for(int i = 0; i < m && i < n ; i++)
	  {
//		  diag.append(","+LU[i][i]);
//		  pivot.append(","+this.piv[i]);
		  if (LU[i][i] == 0)
		  {
//			  zero.append(",true");
			  zeroDiag[i] = true;
			  singular = true;
			  zeroDiagCount++;
		  }
		  else
		  {
//			  zero.append(",false");
			  zeroDiag[i] = false;
		  }
	  }
//	  itsLogger.debug("diag"+diag.toString());
//	  itsLogger.debug("pivot"+pivot.toString());
//	  itsLogger.debug("zeroz"+zero.toString());
      if (B.getRowDimension() != m) {
         throw new IllegalArgumentException("Matrix row dimensions must agree.");
      }

      // Copy right hand side with pivoting
      int nx = B.getColumnDimension();
      Matrix Xmat = B.getMatrix(piv,0,nx-1);
      double[][] X = Xmat.getArray();

      // Solve L*Y = B(piv,:)
      for (int k = 0; k < n; k++) {
         for (int i = k+1; i < n; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      // Solve U*X = Y;
      for (int k = n-1; k >= 0; k--) {
         for (int j = 0; j < nx; j++) {
        	 if(LU[k][k] == 0)
        	 {
        		 X[k][j] = Double.NaN;
        	 }
        	 else
        	 {
        		 X[k][j] /= LU[k][k];
        	 }
         }
         for (int i = 0; i < k; i++) {
            for (int j = 0; j < nx; j++) {
            	if(Double.isNaN(X[k][j]))
            	{
////            		X[i][j] = Double.NaN;//???
//            		if (LU[i][i] == 0)
//            		{X[i][j] = Double.NaN;}

            	}
            	else
            	{
//            		if (LU[i][i] == 0)
//            		{X[i][j] = Double.NaN;}
//            		else
//            		{

            			X[i][j] -= X[k][j]*LU[i][k];
//            		}
            	}
            }
         }
      }
      return Xmat;
   }
   
   
}
