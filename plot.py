import csv
from collections import defaultdict
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

data = defaultdict(list)  # algorithm -> [(V, E, ms)]
with open("results/tables/results.csv") as f:
    for r in csv.DictReader(f):
        data[r["algorithm"]].append((int(r["V"]), int(r["E"]), float(r["avg_time_ms"])))

def plot(index, xlabel, fname):
    plt.figure(figsize=(7, 4.5))
    for algo, pts in data.items():
        plt.plot([p[index] for p in pts], [p[2] for p in pts], marker="o", label=algo)
    plt.xscale("log"); plt.yscale("log")
    plt.xlabel(xlabel); plt.ylabel("Average time (ms), log scale")
    plt.title(f"Execution time vs. {xlabel.lower()}")
    plt.grid(True, which="both", alpha=0.3); plt.legend()
    plt.tight_layout(); plt.savefig(f"results/plots/{fname}", dpi=150); plt.close()

plot(0, "Number of vertices (V)", "time_vs_vertices.png")
plot(1, "Number of edges (E)", "time_vs_edges.png")
print("plots written")
