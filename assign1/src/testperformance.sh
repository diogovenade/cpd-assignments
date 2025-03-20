#!/bin/bash

# File to store the filtered results
OUTPUT_FILE="results.txt"

# Remove previous results file if it exists
if [ -f "$OUTPUT_FILE" ]; then
    rm "$OUTPUT_FILE"
fi

# Compile the C++ code with -O2 optimization flag
echo "Running tests for OnMult from 600 to 3000 with 400 step" >> "$OUTPUT_FILE"
for size in {600..3000..400}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM" >> "$OUTPUT_FILE"
1
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Running tests for OnMultLine from 600 to 3000 with 400 step" >> "$OUTPUT_FILE"
for size in {600..3000..400}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM" >> "$OUTPUT_FILE"
2
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Running tests for OnMultLine from 4096 to 10240 with 2048 step" >> "$OUTPUT_FILE"
for size in {4096..10240..2048}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM" >> "$OUTPUT_FILE"
2
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Running tests for OnMultBlock from 4096 to 10240 with 2048 step" >> "$OUTPUT_FILE"
for size in {4096..10240..2048}
do
    for blockSize in 128 256 512
    do
        echo "Running tests for matrix size: ${size}x${size} with block size: $blockSize" >> "$OUTPUT_FILE"
        ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM" >> "$OUTPUT_FILE"
3
$size
$blockSize
0
EOF
        echo "--------------------------------------------------" >> "$OUTPUT_FILE"
    done
done

echo "Running tests for OnMultLineParA from 600 to 3000 with 400 step" >> "$OUTPUT_FILE"
for size in {600..3000..400}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM|MFLOPS" >> "$OUTPUT_FILE"
4
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Running tests for OnMultLineParA from 4096 to 10240 with 2048 step" >> "$OUTPUT_FILE"
for size in {4096..10240..2048}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM|MFLOPS" >> "$OUTPUT_FILE"
4
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Running tests for OnMultLineParB from 600 to 3000 with 400 step" >> "$OUTPUT_FILE"
for size in {600..3000..400}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM|MFLOPS" >> "$OUTPUT_FILE"
5
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Running tests for OnMultLineParB from 4096 to 10240 with 2048 step" >> "$OUTPUT_FILE"
for size in {4096..10240..2048}
do
    echo "Running tests for matrix size: ${size}x${size}" >> "$OUTPUT_FILE"
    ./matrixproduct <<EOF 2>&1 | grep -E "Time|L1 DCM|L2 DCM|MFLOPS" >> "$OUTPUT_FILE"
5
$size
0
EOF
    echo "--------------------------------------------------" >> "$OUTPUT_FILE"
done

echo "Tests completed. Results saved to $OUTPUT_FILE"