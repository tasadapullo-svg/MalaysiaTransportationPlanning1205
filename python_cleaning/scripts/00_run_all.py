"""
00_run_all.py
一键执行论文数据清洗与 benchmark 复现流程。

执行顺序：
1. TomTom 清洗 + 指标计算 + 日可用率
2. HERE 清洗 + block-time 聚合
3. TomTom 15 min → 30 min + block 聚合
4. TomTom–HERE matched sample 构建
5. benchmark summary
6. sensitivity analyses
7. 可选 Fig 4/Fig 5 基础图形数据

命令示例：
python scripts/00_run_all.py \
  --tomtom_raw data/tomtom_raw.csv \
  --here_raw data/here_raw.csv \
  --point_config config/tomtom_point_config.csv \
  --output_dir output
"""

from __future__ import annotations

import argparse
import importlib.util
from pathlib import Path


def load_module(module_name: str, file_path: Path):
    """从指定 .py 文件动态加载模块。这样可以兼容 01_xxx.py 这种文件名。"""
    spec = importlib.util.spec_from_file_location(module_name, file_path)
    if spec is None or spec.loader is None:
        raise ImportError(f"无法加载模块: {file_path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def run_all(tomtom_raw: Path, here_raw: Path, point_config: Path, output_dir: Path, require_confidence: bool = False, peak_q: float = 0.85) -> None:
    scripts_dir = Path(__file__).resolve().parent
    output_dir.mkdir(parents=True, exist_ok=True)

    tomtom_mod = load_module("tomtom_cleaning", scripts_dir / "01_tomtom_cleaning.py")
    here_mod = load_module("here_cleaning", scripts_dir / "02_here_cleaning.py")
    bench_mod = load_module("tomtom_here_benchmark", scripts_dir / "03_tomtom_here_benchmark.py")
    sens_mod = load_module("sensitivity_analysis", scripts_dir / "04_sensitivity_analysis.py")
    plot_mod = load_module("plot_reproduction", scripts_dir / "05_plot_reproduction.py")

    # 1. TomTom 清洗。
    tom_raw = tomtom_mod.read_table(tomtom_raw)
    tom_cleaned, tom_daily, tom_cleaning_log, tom_dup_log = tomtom_mod.clean_tomtom(tom_raw, require_confidence=require_confidence)
    tomtom_mod.write_csv(tom_cleaned, output_dir / "tomtom_cleaned_point_time.csv")
    tomtom_mod.write_csv(tom_daily, output_dir / "tomtom_daily_availability.csv")
    tomtom_mod.write_csv(tom_cleaning_log, output_dir / "tomtom_cleaning_log_by_date.csv")
    tomtom_mod.write_csv(tom_dup_log, output_dir / "tomtom_duplicate_log.csv")

    # 2. HERE 清洗。
    here_raw_df = here_mod.read_table(here_raw)
    here_block, here_log = here_mod.clean_here(here_raw_df)
    here_mod.write_csv(here_block, output_dir / "here_cleaned_block_time.csv")
    here_mod.write_csv(here_log, output_dir / "here_cleaning_log_by_date.csv")

    # 3. Benchmark 匹配。
    cfg = bench_mod.read_table(point_config)
    tom_block = bench_mod.aggregate_tomtom_to_30min_blocks(tom_cleaned, cfg)
    matched = bench_mod.match_tomtom_here(tom_block, here_block)
    bench_summary = bench_mod.benchmark_summary(matched, q=peak_q)
    bench_mod.write_csv(tom_block, output_dir / "tomtom_block_30min.csv")
    bench_mod.write_csv(matched, output_dir / "matched_tomtom_here_30min.csv")
    bench_mod.write_csv(bench_summary, output_dir / "benchmark_summary.csv")

    # 4. 敏感性分析。
    sens_results = sens_mod.run_all_sensitivity(matched, q=peak_q)
    for name, df in sens_results.items():
        sens_mod.write_csv(df, output_dir / f"{name}.csv")

    # 5. 图形基础复现数据。
    profile, day_type_profile = plot_mod.build_intraday_profile(tom_cleaned)
    profile = plot_mod.add_standardized_profile(profile)
    plot_mod.write_csv(profile, output_dir / "fig5_intraday_profile_data.csv")
    plot_mod.write_csv(day_type_profile, output_dir / "fig5_weekday_weekend_profile_data.csv")
    plot_mod.save_basic_plots(tom_daily, profile, output_dir)

    # 6. 控制台复核。
    print("全部流程执行完成。")
    print(f"输出目录: {output_dir}")
    print(f"TomTom cleaned records: {len(tom_cleaned):,}")
    print(f"HERE block-time records: {len(here_block):,}")
    print(f"Matched TomTom–HERE records: {len(matched):,}")
    print("Benchmark summary:")
    print(bench_summary[["unit", "matched_n", "pearson_r", "spearman_rho", "peak_overlap_index"]])


def run_cli() -> None:
    parser = argparse.ArgumentParser(description="一键执行论文 Python 数据清洗与 benchmark 复现流程。")
    parser.add_argument("--tomtom_raw", required=True, help="TomTom raw table 路径。")
    parser.add_argument("--here_raw", required=True, help="HERE raw table 路径。")
    parser.add_argument("--point_config", required=True, help="TomTom point → block 配置表。")
    parser.add_argument("--output_dir", required=True, help="输出目录。")
    parser.add_argument("--require_confidence", action="store_true", help="是否强制要求 confidence 非空。")
    parser.add_argument("--peak_q", type=float, default=0.85, help="Peak overlap 主阈值，默认 0.85。")
    args = parser.parse_args()

    run_all(
        tomtom_raw=Path(args.tomtom_raw),
        here_raw=Path(args.here_raw),
        point_config=Path(args.point_config),
        output_dir=Path(args.output_dir),
        require_confidence=args.require_confidence,
        peak_q=args.peak_q,
    )


if __name__ == "__main__":
    run_cli()
