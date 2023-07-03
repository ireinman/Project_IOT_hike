import numpy as np
from scipy import interpolate
from scipy import signal

def is_up(times, acc):  # times >= 0, acc = acc - 9.81
    dist = 3.5
    step = 0.24
    max_height = -1.65
    min_height = 1.6
    if len(times) > len(acc):
        times = times[:-1]
    # data = np.array([times, acc])
    tck = interpolate.splrep(times, acc, s=0)
    xnew = np.arange(times[0], times[-1], step)
    ynew = interpolate.splev(xnew, tck, der=0)
    peaks = signal.find_peaks(ynew, distance=dist, height=max_height)[0]
    bottoms = signal.find_peaks(-ynew, distance=dist, height=min_height)[0]
    steps = 0
    extreme_points = sorted(np.concatenate((peaks, bottoms), axis=None))
    start_point = 0
    while start_point < len(extreme_points) and extreme_points[start_point] in peaks:
        start_point += 1
    i = start_point
    while i <len(extreme_points) - 1:
        if extreme_points[i] in bottoms and extreme_points[i + 1] in peaks:
            if ynew[extreme_points[i]] + 2 < ynew[extreme_points[i + 1]]:
                steps += 1
            else:
                del extreme_points[i + 1]
                i -= 1
        if extreme_points[i] in peaks and extreme_points[i + 1] in bottoms:
            if ynew[extreme_points[i]] - 2 < ynew[extreme_points[i + 1]]:
                del extreme_points[i + 1]
                i -= 1
        i += 1
    if len(extreme_points) > 0 and extreme_points[len(extreme_points) - 1] in bottoms:
        return [steps, 1]
    else:
        return [steps, 0]


def extract_data(times, acc, total_push_ups):
    total_time = times[-1]
    tck = interpolate.splrep(times, acc, s=0)
    xnew = np.arange(times[0], total_time, 0.2)
    ynew = interpolate.splev(xnew, tck, der=0)
    max_acc = max(ynew)
    avg_time_per_push_up = total_push_ups / total_time
    return [max_acc, avg_time_per_push_up]

def extract_data_bsu(times, acc):
    total_time = times[-1]
    tck = interpolate.splrep(times, acc, s=0)
    xnew = np.arange(times[0], total_time, 0.2)
    ynew = interpolate.splev(xnew, tck, der=0)
    max_acc = max(ynew)
    return max_acc

def bsu_up(times,acc,starting_up,ending_up):
    data = np.array([times,acc]).T
    tck = interpolate.splrep(data[:,0], data[:,1], s=0)
    xnew = np.arange(starting_up, ending_up, 0.2)
    ynew = interpolate.splev(xnew, tck, der=0)
    peaks = signal.find_peaks(ynew)[0]
    bottoms = signal.find_peaks(-ynew)[0]
    for peak in peaks:
        for bottom in bottoms:
            if peak < bottom:
                if ynew[peak] > ynew[bottom] + 1:
                    return True
    return False
