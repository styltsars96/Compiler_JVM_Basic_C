
void print_array(int [] a, int n) { 
    int s;
    s = 0;
    while(s<n) {
        print(a[s]);
        print(" ");
        s = s+1;
    }
    print("\n");
}

void main() {

    int [] x;
    x = new int[10];

    int i;
    i = 1;
    x[0]=0;
    while(i<10) {
        x[i]=x[i-1]*i;
        i=i+1;
    }

    print_array(x, 10);
    
}