import re
import json

def parse_corpus():
    with open('app/src/main/java/com/example/duniya/data/DuniyaResearchCorpus.kt', 'r', encoding='utf-8') as f:
        src = f.read()

    # Parse benchmarks
    bench_section = src.split('val hardBenchmarkPrompts')[1].split('val articles')[0]
    benchmarks = []
    # Pattern to match each HardBenchmarkPrompt
    bench_blocks = bench_section.split('HardBenchmarkPrompt(')[1:]
    for b in bench_blocks:
        def get_field(name):
            m = re.search(r'' + name + r'\s*=\s*"([^"]+)"', b)
            return m.group(1) if m else ""
        benchmarks.append({
            "id": get_field("id"),
            "category": get_field("category"),
            "title": get_field("title"),
            "prompt": get_field("prompt"),
            "mode": get_field("mode"),
            "why1BFailsShort": get_field("why1BFailsShort"),
            "simulated1BOutput": get_field("simulated1BOutput")
        })

    # Articles
    art_section = src.split('val articles: List<ResearchArticle> = listOf(')[1]
    art_blocks = art_section.split('ResearchArticle(')[1:]
    articles = []
    for p in art_blocks:
        def get_field(name):
            m = re.search(r'' + name + r'\s*=\s*"([^"]+)"', p)
            return m.group(1) if m else ""

        art_id = get_field("id")
        title = get_field("title")
        if not art_id or not title:
            continue

        domain = get_field("domain")
        subcategory = get_field("subcategory")
        summary = get_field("summary")

        tags_m = re.search(r'tags\s*=\s*listOf\((.*?)\)', p, re.DOTALL)
        tags = [t.strip().strip('"') for t in tags_m.group(1).split(',')] if tags_m else []

        deep_m = re.search(r'deepExplanation\s*=\s*"""(.*?)"""\.trimIndent\(\)', p, re.DOTALL)
        deepExplanation = deep_m.group(1).strip() if deep_m else ""
        if not deepExplanation:
            deepExplanation = get_field("deepExplanation")

        firstPrinciples = get_field("firstPrinciplesMathOrMechanism")

        # structuredMetrics = mapOf(...)
        metrics = {}
        metrics_m = re.search(r'structuredMetrics\s*=\s*mapOf\((.*?)\)\s*,', p, re.DOTALL)
        if metrics_m:
            pairs = re.findall(r'"([^"]+)"\s*to\s*"([^"]+)"', metrics_m.group(1))
            for k, v in pairs:
                metrics[k] = v

        tradeOffs = get_field("tradeOffsAndEdgeCases")
        failureMode = get_field("oneBModelFailureMode")

        citations_m = re.search(r'primaryCitations\s*=\s*listOf\((.*?)\)\s*,', p, re.DOTALL)
        citations = []
        if citations_m:
            citations = [c.strip().strip('"') for c in re.findall(r'"([^"]+)"', citations_m.group(1))]

        related_m = re.search(r'relatedIds\s*=\s*listOf\((.*?)\)', p, re.DOTALL)
        relatedIds = []
        if related_m:
            relatedIds = [r.strip().strip('"') for r in re.findall(r'"([^"]+)"', related_m.group(1))]

        articles.append({
            "id": art_id,
            "title": title,
            "domain": domain,
            "subcategory": subcategory,
            "tags": tags,
            "summary": summary,
            "deepExplanation": deepExplanation,
            "firstPrinciplesMathOrMechanism": firstPrinciples,
            "structuredMetrics": metrics,
            "tradeOffsAndEdgeCases": tradeOffs,
            "oneBModelFailureMode": failureMode,
            "primaryCitations": citations,
            "relatedIds": relatedIds
        })

    return benchmarks, articles

if __name__ == '__main__':
    b, a = parse_corpus()
    print(f"Parsed {len(b)} benchmarks and {len(a)} articles.")
    with open('scripts/parsed_corpus.json', 'w', encoding='utf-8') as out:
        json.dump({"benchmarks": b, "articles": a}, out, indent=2)
