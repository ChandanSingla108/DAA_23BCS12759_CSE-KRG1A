class Solution {
    public double myPow(double x, int n) {
       if(n==0 || x == 1){
        return 1;
       }
       if(n<0){
        long n1 = n*(long)-1;  // int * long = long;
        double ans = power(x,n1);
        return 1/ans;
       }else{
        double ans = power(x,n);
        return ans;
       }

    }
    public double power(double x,long n){
        if(n==1){
            return x;
        }
        double temp = power(x,n/2);
        if(n%2==0){
            return temp*temp;
        }
        else{
            return temp*temp*x;
        }
    }
}
