# text_to_image.py – Stable Diffusion v1.5 with local HuggingFace cache
import os
from pathlib import Path

import torch
from diffusers import StableDiffusionPipeline

from hf_cache import setup_cache

# Initialize HF_HOME and HF_HUB_CACHE
setup_cache()

hf_home = os.environ["HF_HOME"]
local_model_path = os.path.join(
    hf_home,
    "hub",
    "models--runwayml--stable-diffusion-v1-5"
)

print(f"The model will be stored : {local_model_path}")

# download the model (if not exists) and put it in local cache
print("download the model (if not exists) ...")
pipe = StableDiffusionPipeline.from_pretrained(
    "runwayml/stable-diffusion-v1-5",
    torch_dtype=torch.float32,
)

pipe.save_pretrained(local_model_path)

print(f"The model is stored: {local_model_path}")

# Use the model NOW from the local folder
print("Download the model NOW from the local folder ...")
pipe_local = StableDiffusionPipeline.from_pretrained(
    local_model_path,
    torch_dtype=torch.float32
)

# Then generate the picture
# prompt = "A 35mm macro photo of 3 cute puppies and a tiger in a field"
prompt = "Digital art of a young girl sitting in a garden with a one black dog that looks like a fox. The girl is smiling and the dog is wagging its tail. The image has a hazy, dream-like quality, with crackly film effects to evoke nostalgia."
negative="blurry, low quality, low resolution, watermark, text, signature,extra fingers, extra limbs, deformed, mutated, distorted, bad anatomy,worst quality, jpeg artifacts, grainy, noisy, bad hands, bad face,oversaturated, underexposed, overexposed"
print("Generate the picture...")
image = pipe_local(
    prompt=prompt,
    negative_prompt=negative,
    num_inference_steps=100,  # originally 30
    guidance_scale=20,
    width=512,
    height=512,
    generator=torch.Generator("cpu").manual_seed(4321), # seed 1234
).images[0]

output_dir = Path("src/main/resources/ch04/images")
output_dir.mkdir(parents=True, exist_ok=True)
output_file = output_dir / "puppies.png"
# output_file = r"E:\A\ai\GPTforJava\src\main\resources\ch04\images\puppies3-4-steps100_guidance_scale20_seed4321.png"
image.save(output_file)

print(f"The picture is stored: {output_file}")

# set HF_HOME=E:\cach\huggingface
# set HF_HUB_CACHE=E:\cach\huggingface\hub
# python text_to_image.py


