# hf_cache.py – auto move cash Hugging Face :
import os
import sys
from pathlib import Path

def setup_cache():
    """Setup Hugging Face cache only for local development."""

    # check Docker enviroment
    if os.path.exists('/.dockerenv'):
        print("Running in Docker, skipping local cache setup")
        return

    # for Windows set up the paths
    if os.name == 'nt':
        os.environ["HF_HOME"] = "E:/cach/huggingface"
        os.environ["HF_HUB_CACHE"] = "E:/cach/huggingface/hub"
        Path(os.environ["HF_HOME"]).mkdir(parents=True, exist_ok=True)
        print(f"Hugging Face cache → {os.environ['HF_HOME']}")
    else:
        # not Windows и not Docker - cancel with error
        print("   Error: This script is only for Windows local development or Docker")
        print("   Detected OS:", os.name)
        sys.exit(1)
if __name__ == "__main__":
    setup_cache()
