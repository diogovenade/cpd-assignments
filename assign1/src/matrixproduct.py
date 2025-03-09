import time

def on_mult(m_ar, m_br):
    pha = [1.0] * (m_ar*m_ar)
    phb = [(i+1) for i in range(m_br) for _ in range(m_br)]
    phc = [0.0] * (m_ar*m_ar)
    
    start_time = time.time()
    
    for i in range(m_ar):
        for j in range(m_br):
            temp = 0.0
            for k in range(m_ar):
                temp += pha[i*m_ar+k] * phb[k*m_br+j]
            phc[i*m_br+j] = temp
    
    end_time = time.time()
    print(f"Time: {end_time - start_time:.3f} seconds")
    
    # display 10 elements of the result matrix tto verify correctness
    print("Result matrix:")
    print(" ".join(f"{phc[j]:.1f}" for j in range(min(10, m_br))))

# on_mult(600, 600)
# on_mult(1000, 1000)
# on_mult(1400, 1400)
# on_mult(1800, 1800)
# on_mult(2200, 2200)
# on_mult(2600, 2600)
# on_mult(3000, 3000)