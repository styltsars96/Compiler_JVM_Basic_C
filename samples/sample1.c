

int main() { 

    int i;
    i = 0;
    while(i < 100) { 
        print("i=");
        print(i);
        print('\n');
        i = i + 1;
    }

    if (i == 100) { 
        print("Done\n");
    } else { 
        print("Bug!");
    }
	return 0;

}