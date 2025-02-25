import time

def on_mult(m_ar, m_br):
    pha = [[1.0 for _ in range(m_ar)] for _ in range(m_ar)]
    phb = [[i + 1 for _ in range(m_br)] for i in range(m_br)]
    phc = [[0.0 for _ in range(m_br)] for _ in range(m_ar)]
    
    start_time = time.time()
    
    for i in range(m_ar):
        for j in range(m_br):
            temp = 0.0
            for k in range(m_ar):
                temp += pha[i][k] * phb[k][j]
            phc[i][j] = temp
    
    end_time = time.time()
    print(f"Time: {end_time - start_time:.3f} seconds")
    
    # display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    print(" ".join(f"{phc[0][j]:.1f}" for j in range(min(10, m_br))))

on_mult(500, 500)