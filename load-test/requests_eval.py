import argparse
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np
import math


def round_to_first_digit(numbers):
    rounded_numbers = []
    for number in numbers:
        if number == 0:
            rounded_numbers.append(0)
        else:
            exponent = math.floor(math.log10(abs(number)))
            rounded_number = round(number / (10 ** exponent)) * (10 ** exponent)
            rounded_numbers.append(rounded_number)
    return rounded_numbers


def main(file_path):
    data = pd.read_csv(file_path)

    # Extract data from all columns and find the min and max values
    all_data = data.values.flatten()
    min_value = all_data.min()
    max_value = all_data.max()

    # Calculate logarithmically spaced ticks between the min and max values
    log_min = np.log10(min_value)
    log_max = np.log10(max_value)
    ticks = round_to_first_digit(np.logspace(log_min, log_max, num=5))

    # Create a plot comparing the execution times for all columns
    plt.figure(figsize=(20, 6))
    for i in range(data.shape[1]):
        line, = plt.plot(data.iloc[:, i], label=data.columns[i], marker='o')
        avg_value = data.iloc[:, i].mean()
        plt.axhline(y=avg_value, color=line.get_color(), linestyle='--', alpha=0.5, label=f'Avg {data.columns[i]}')

    plt.yscale('log')
    plt.title('Execution Times Comparison')
    plt.xlabel('Run')
    plt.ylabel('Time (ms)')
    plt.legend()

    ax = plt.gca()
    ax.set_yticks(ticks)
    ax.get_yaxis().set_major_formatter(ticker.ScalarFormatter())
    ax.set_yticklabels([str(tick) for tick in ticks])

    plt.grid(True, which="both", ls="--")
    plt.show()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Plot data from CSV file.")
    parser.add_argument("-f", "--file", metavar='', type=str, default="./load-test/responses.csv",
                        help="Path to the CSV file (default: ./load-test/responses.csv)")
    args = parser.parse_args()

    main(args.file)
