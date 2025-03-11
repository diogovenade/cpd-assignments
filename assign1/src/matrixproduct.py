import time
import numpy as np

def on_mult(m_ar, m_br):
    """Standard matrix multiplication algorithm (i,j,k order)"""
    # Initialize matrices
    pha = [1.0] * (m_ar * m_ar)
    phb = [0.0] * (m_br * m_br)
    phc = [0.0] * (m_ar * m_br)
    
    # Initialize matrix B properly (each row has same value i+1)
    for i in range(m_br):
        for j in range(m_br):
            phb[i * m_br + j] = i + 1
    
    start_time = time.time()
    
    for i in range(m_ar):
        for j in range(m_br):
            temp = 0.0
            for k in range(m_ar):
                temp += pha[i*m_ar+k] * phb[k*m_br+j]
            phc[i*m_ar+j] = temp
    
    end_time = time.time()
    print(f"Time: {end_time - start_time:.3f} seconds")
    
    # Display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    print(" ".join(f"{phc[j]}" for j in range(min(10, m_br))))

def on_mult_line(m_ar, m_br):
    """Line-by-line matrix multiplication algorithm (i,k,j order)"""
    # Initialize matrices
    pha = [1.0] * (m_ar * m_ar)
    phb = [0.0] * (m_br * m_br)
    phc = [0.0] * (m_ar * m_br)
    
    # Initialize matrix B
    for i in range(m_br):
        for j in range(m_br):
            phb[i * m_br + j] = i + 1
    
    start_time = time.time()
    
    # Line multiplication algorithm (matches C++ OnMultLine)
    for i in range(m_ar):
        for k in range(m_ar):
            val_a = pha[i * m_ar + k]
            for j in range(m_br):
                phc[i * m_ar + j] += val_a * phb[k * m_br + j]
    
    end_time = time.time()
    print(f"Time: {end_time - start_time:.3f} seconds")
    
    # Display 10 elements of the result matrix
    print("Result matrix:")
    print(" ".join(f"{phc[j]}" for j in range(min(10, m_br))))

def on_mult_block(m_ar, m_br, bk_size):
    """Block-by-block matrix multiplication algorithm"""
    # Initialize matrices
    pha = [1.0] * (m_ar * m_ar)
    phb = [0.0] * (m_br * m_br)
    phc = [0.0] * (m_ar * m_br)
    
    # Initialize matrix B
    for i in range(m_br):
        for j in range(m_br):
            phb[i * m_br + j] = i + 1
    
    start_time = time.time()
    
    # Block multiplication algorithm (matches C++ OnMultBlock)
    for i in range(0, m_ar, bk_size):
        for j in range(0, m_br, bk_size):
            for k in range(0, m_ar, bk_size):
                # Process blocks
                for l in range(i, min(i + bk_size, m_ar)):
                    for m in range(k, min(k + bk_size, m_ar)):
                        temp = pha[l * m_ar + m]
                        for n in range(j, min(j + bk_size, m_br)):
                            phc[l * m_ar + n] += temp * phb[m * m_br + n]
    
    end_time = time.time()
    print(f"Time: {end_time - start_time:.3f} seconds")
    
    # Display 10 elements of the result matrix
    print("Result matrix:")
    print(" ".join(f"{phc[j]}" for j in range(min(10, m_br))))

def main():
    """Main function to handle user inputs similar to C++ version"""
    print("Matrix Multiplication")
    print("1. Standard matrix multiplication")
    print("2. Line-by-line matrix multiplication")
    print("3. Block-by-block matrix multiplication")
    choice = int(input("Enter choice: "))
    
    if choice == 1:
        m_ar = int(input("Enter matrix A rows: "))
        m_br = int(input("Enter matrix B rows: "))
        print("Calculating...")
        on_mult(m_ar, m_br)
    elif choice == 2:
        m_ar = int(input("Enter matrix A rows: "))
        m_br = int(input("Enter matrix B rows: "))
        print("Calculating...")
        on_mult_line(m_ar, m_br)
    elif choice == 3:
        m_ar = int(input("Enter matrix A rows: "))
        m_br = int(input("Enter matrix B rows: "))
        bk_size = int(input("Enter block size: "))
        print("Calculating...")
        on_mult_block(m_ar, m_br, bk_size)
    else:
        print("Invalid choice")

if __name__ == "__main__":
    main()