import json
from SPARQLWrapper import SPARQLWrapper, JSON
from fastapi import FastAPI 
from pydantic import BaseModel
from typing import List, Optional
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

class EntityLinkingRequest(BaseModel):
    name: str
    value: str

class ResponseEntity(BaseModel):
    label: str
    resource: str

def query_dbpedia_ru(entity_label, language="ru", limit=5):

    query = f"""
    SELECT ?resource ?label ?abstract WHERE {{
      ?resource rdfs:label "{entity_label}"@{language}.
      OPTIONAL {{
          ?resource dbo:abstract ?abstract.
          FILTER(LANG(?abstract) = "{language}")
      }}
      OPTIONAL {{
          ?resource rdfs:label ?label.
          FILTER(LANG(?label) = "{language}")
      }}
    }}
    LIMIT {limit}
    """
    sparql = SPARQLWrapper("http://dbpedia.org/sparql")
    sparql.setReturnFormat(JSON)
    sparql.setQuery(query)

    try:
        return sparql.query().convert()
    except Exception as e:
        print(f"Ошибка DBpedia для '{entity_label}': {e}")
        return None

def query_wikidata_ru(entity_label, language="ru", limit=5):

    query = f"""
    SELECT ?item ?itemLabel ?description ?instanceOfLabel WHERE {{
      ?item rdfs:label "{entity_label}"@{language}.
      OPTIONAL {{
          ?item schema:description ?description.
          FILTER(LANG(?description)="{language}")
      }}
      OPTIONAL {{
          ?item wdt:P31 ?instanceOf.
          ?instanceOf rdfs:label ?instanceOfLabel.
          FILTER(LANG(?instanceOfLabel)="{language}")
      }}
      SERVICE wikibase:label {{ bd:serviceParam wikibase:language "{language}". }}
    }}
    LIMIT {limit}
    """
    sparql = SPARQLWrapper("https://query.wikidata.org/sparql")
    sparql.setReturnFormat(JSON)
    sparql.setQuery(query)

    try:
        return sparql.query().convert()
    except Exception as e:
        print(f"Ошибка Wikidata для '{entity_label}': {e}")
        return None

@app.post("/link_entities", response_model=List[ResponseEntity])
async def link_entities_combined(entity_list: List[EntityLinkingRequest]):
    combined_results = []
    limit = 5
    for entity in entity_list:
        print(f"\nОбработка сущности: '{entity.value}'")

        dbpedia_results = query_dbpedia_ru(entity.value, limit=limit)
        if dbpedia_results and "results" in dbpedia_results:
            for result in dbpedia_results["results"]["bindings"]:
                label = result.get("label", {}).get("value", entity.value)
                resource = result.get("resource", {}).get("value", "")
                combined_results.append({
                    "label": label,
                    "resource": resource
                })

        wikidata_results = query_wikidata_ru(entity.value, limit=limit)
        if wikidata_results and "results" in wikidata_results:
            for result in wikidata_results["results"]["bindings"]:
                label = result.get("itemLabel", {}).get("value", entity.value)
                resource = result.get("item", {}).get("value", "")
                combined_results.append({
                    "label": label,
                    "resource": resource
                })

    return combined_results

