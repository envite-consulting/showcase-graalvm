import argparse
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np
import math

image_mapping = {
    "jvm": "jvm",
    "jlink": "jvm-jlink",
    "cds": "jvm-jlink-cds",
    "aot": "jvm-jlink-cds-aot",
    "native": "native"
}


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


def smooth_data(data, window_size=5):
    return data.rolling(window=window_size, min_periods=1, center=True).mean()


def main(file_path, smooth=False):
    column_names = [image_mapping[images] for images in selected_images if images in image_mapping]
    default_images = ["jvm", "native"]
    if not column_names:
        print(f"Invalid image names provided. Using default images: {default_images}")
        column_names = default_images

    data = pd.read_csv(file_path, usecols=column_names)

    if smooth:
        data = data.apply(smooth_data)

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
    parser.add_argument("-i", "--images", metavar='', type=str, required=True,
                        help="Comma-separated list of image short names (e.g., 'jvm,cds,native').")
    parser.add_argument("-f", "--file", metavar='', type=str, default="./load-test/responses.csv",
                        help="Path to the CSV file (default: ./load-test/responses.csv)")
    parser.add_argument("-s", "--smooth", action="store_true",
                        help="Enable smoothing for the graph.")
    args = parser.parse_args()
    selected_images = args.images.split(',')

    main(args.file, smooth=args.smooth)
