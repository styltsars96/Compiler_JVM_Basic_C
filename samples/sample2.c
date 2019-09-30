
int sum(int x) {
    if (x <= 0) { 
        return x;
    }
    return x + sum(x-1);
}

void main() { 

    int i;
    i = 3;

    int j;
    j = 7;

    int s;
    s = sum(i+j);

    print("sum=");
    print(s);
    print("\n");
}