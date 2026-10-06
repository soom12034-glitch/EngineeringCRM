from pathlib import Path
import os
import subprocess
import sys

root = Path(__file__).resolve().parent
tests = sorted(root.glob("*_test.py"))
environment = os.environ.copy()
environment["PYTHONIOENCODING"] = "utf-8"
for test in tests:
    print(f"Running {test.name}", flush=True)
    subprocess.run([sys.executable, str(test)], check=True, env=environment)
print(f"PASS: {len(tests)} Python migration tests.")
