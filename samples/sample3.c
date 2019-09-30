
int sum(int x) {
    if (x <= 0) { 
        return x;
    }
    return x + sum(x-1);
}

void display_sum(int x) { 
    int s;
    s = sum(x);
    print("sum=");
    print(s);
    print("\n");
}

void main() { 

    display_sum(100);
    
}