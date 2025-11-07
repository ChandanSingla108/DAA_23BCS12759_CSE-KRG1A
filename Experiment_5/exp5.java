

class Solution {
    // Function to sort an array using quick sort algorithm.
    static void quickSort(int arr[], int low, int high) {
        if(low>= high) return;
        int p = partition(arr,low,high);
        quickSort(arr,low,p-1);
        quickSort(arr,p+1,high);
    }

    static int partition(int arr[], int low, int high) {
       int i = low+1;
       int j = high;
       while(i<=j){
           if(arr[i]<=arr[low]) i++;
           else if(arr[j]>arr[low]) j--;
           else{
               int temp = arr[i];
               arr[i] = arr[j];
               arr[j] = temp;
               i++;
               j--;
           }
       }
       int temp = arr[low];
       arr[low] = arr[j];
       arr[j] = temp;
       return j;
    }
}
