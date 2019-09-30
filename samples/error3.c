
void bar1(int x){
    return;
}

void foo(double x, double y, int z) {
    bar(z);
    return;
}

int main() { 

    int x;
    x = 2.5;

    foo(x, x, x);

}