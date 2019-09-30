
void main() {

    int n;
    int i;
    int is_prime;

    n = 2;
	
    while(n < 100) { 
        i = 2;
        is_prime = 1;
		
        while(i < n) { 
            if (n%i == 0) {
                is_prime = 0; 
                break;
            }
            i = i + 1;
        }
		
        if (is_prime == 1) {
            print(n);
            print(" ");
        }
		
        n = n + 1;
    }
	
    print("\n");
	return;
    
}