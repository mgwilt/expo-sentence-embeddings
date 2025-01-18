import torch
from transformers import AutoModel, AutoTokenizer
import torch.nn as nn

class ModelWrapper(nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, input_ids):
        # Get only the last hidden state from the model output
        return self.model(input_ids).last_hidden_state

def download_and_convert_model():
    # Load model and tokenizer
    model_name = "sentence-transformers/all-MiniLM-L6-v2"
    model = AutoModel.from_pretrained(model_name)
    tokenizer = AutoTokenizer.from_pretrained(model_name)

    # Save tokenizer files
    tokenizer.save_pretrained("../android/src/main/assets/tokenizer")

    # Wrap the model to get only the embeddings
    wrapped_model = ModelWrapper(model)
    wrapped_model.eval()

    # Convert model to TorchScript
    example_input = torch.zeros((1, 32), dtype=torch.long)  # Example input tensor
    with torch.no_grad():
        traced_model = torch.jit.trace(wrapped_model, example_input)

    # Save the model
    traced_model.save("../android/src/main/assets/all-minilm-l6-v2.pt")

if __name__ == "__main__":
    download_and_convert_model() 