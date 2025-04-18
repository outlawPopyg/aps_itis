from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from gliner import GLiNER
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

origins = [
    "http://localhost",
    "http://localhost:8081",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class EntityRequest(BaseModel):
    name: str
    description: str

class ExtractionRequest(BaseModel):
    text: str
    entities: List[EntityRequest]

class ResponseEntity(BaseModel):
    name: str
    value: str

model = None

@app.on_event("startup")
async def load_model():
    global model
    model = GLiNER.from_pretrained("gliner-community/gliner_large-v2.5")

@app.post("/extract", response_model=List[ResponseEntity])
async def extract_entities(request: ExtractionRequest):
    labels = [e.description for e in request.entities]
    result = []
    
    entities = model.predict_entities(request.text, labels, threshold=0.5)
        
    for entity in entities:
        foundName = ""
        for i in request.entities:
            if i.description == entity["label"]:
                foundName = i.name
        result.append(
            ResponseEntity(
                name=foundName,
                value=entity["text"]
            )
        )
        
    
    return result
