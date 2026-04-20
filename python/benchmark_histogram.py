import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np

plt.rcParams.update({
    "font.family": "serif",
    "font.size": 11,
    "axes.titlesize": 12,
    "axes.labelsize": 11,
    "axes.linewidth": 0.8,
    "axes.spines.top": False,
    "axes.spines.right": False,
    "xtick.labelsize": 10,
    "ytick.labelsize": 10,
    "xtick.direction": "out",
    "ytick.direction": "out",
    "xtick.major.size": 4,
    "ytick.major.size": 4,
    "figure.dpi": 300,
    "savefig.dpi": 300,
    "savefig.bbox": "tight",
})

COLOR = "#2166ac"

df = pd.read_csv("benchmark_distribution.csv")

fig, axes = plt.subplots(1, 3, figsize=(12, 3.5))

# --- Panel A: Query mass distribution ---
ax = axes[0]
ax.hist(df["mass"], bins=80, color=COLOR, edgecolor="white", linewidth=0.3)
ax.set_xlabel("Peptide mass (Da)")
ax.set_ylabel("Number of queries")
ax.xaxis.set_major_locator(ticker.MultipleLocator(2000))
ax.set_xlim(left=0)
ax.text(-0.18, 1.02, "A", transform=ax.transAxes, fontsize=13, fontweight="bold")

# --- Panel B: Hits distribution ---
ax = axes[1]
ax.hist(df["hit_count"], bins=80, color=COLOR, edgecolor="white", linewidth=0.3)
ax.set_xlabel("Peptide hits per query")
ax.set_ylabel("Number of queries")
ax.xaxis.set_major_formatter(ticker.FuncFormatter(
    lambda x, _: f"{x/1e6:.1f}M" if x >= 1e6 else f"{int(x/1e3)}k" if x >= 1e3 else str(int(x))
))
ax.set_xlim(left=0)
ax.text(-0.18, 1.02, "B", transform=ax.transAxes, fontsize=13, fontweight="bold")

# --- Panel C: Latency distribution ---
ax = axes[2]
ax.hist(df["latency_ms"], bins=80, color=COLOR, edgecolor="white", linewidth=0.3)
ax.set_xlabel("Query latency (ms)")
ax.set_ylabel("Number of queries")
ax.set_xlim(left=0)
ax.text(-0.18, 1.02, "C", transform=ax.transAxes, fontsize=13, fontweight="bold")

median_lat = np.median(df["latency_ms"])
p95_lat = np.percentile(df["latency_ms"], 95)
axes[2].axvline(median_lat, color="#d73027", linewidth=1.2, linestyle="--",
                label=f"Median ({median_lat:.0f} ms)")
axes[2].axvline(p95_lat,    color="#fc8d59", linewidth=1.2, linestyle=":",
                label=f"P95 ({p95_lat:.0f} ms)")
axes[2].legend(fontsize=9, frameon=False)

plt.tight_layout(w_pad=2.5)
plt.savefig("benchmark_histogram.pdf", format="pdf")
plt.savefig("benchmark_histogram.png", format="png")
plt.show()