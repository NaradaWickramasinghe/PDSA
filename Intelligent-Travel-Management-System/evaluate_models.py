import numpy as np
import random

random.seed(42)
np.random.seed(42)

PERSONAS = [
    ('Ella', {'budget': (400, 900), 'duration': (2, 6), 'group': (1, 4), 'adv': (8, 10), 'nat': (8, 10), 'bch': (1, 3), 'cul': (2, 5), 'nit': (3, 7), 'rel': (4, 8)}),
    ('Mirissa', {'budget': (500, 1100), 'duration': (3, 7), 'group': (2, 6), 'adv': (4, 7), 'nat': (5, 8), 'bch': (8, 10), 'cul': (1, 4), 'nit': (7, 10), 'rel': (6, 9)}),
    ('Sigiriya', {'budget': (450, 950), 'duration': (1, 4), 'group': (1, 5), 'adv': (6, 9), 'nat': (6, 9), 'bch': (1, 2), 'cul': (8, 10), 'nit': (1, 3), 'rel': (4, 7)}),
    ('Kandy', {'budget': (400, 850), 'duration': (2, 5), 'group': (1, 6), 'adv': (2, 5), 'nat': (5, 8), 'bch': (1, 2), 'cul': (8, 10), 'nit': (3, 6), 'rel': (5, 8)}),
    ('Nuwara Eliya', {'budget': (600, 1300), 'duration': (2, 5), 'group': (2, 5), 'adv': (3, 6), 'nat': (8, 10), 'bch': (1, 2), 'cul': (4, 7), 'nit': (2, 5), 'rel': (8, 10)}),
    ('Arugam Bay', {'budget': (350, 800), 'duration': (3, 8), 'group': (1, 4), 'adv': (8, 10), 'nat': (4, 7), 'bch': (8, 10), 'cul': (1, 3), 'nit': (6, 9), 'rel': (5, 8)}),
    ('Yala National Park', {'budget': (700, 1600), 'duration': (1, 4), 'group': (2, 6), 'adv': (7, 10), 'nat': (9, 10), 'bch': (1, 3), 'cul': (1, 3), 'nit': (1, 2), 'rel': (3, 6)}),
    ('Galle Fort', {'budget': (550, 1200), 'duration': (1, 4), 'group': (2, 4), 'adv': (2, 5), 'nat': (3, 6), 'bch': (6, 8), 'cul': (8, 10), 'nit': (6, 8), 'rel': (7, 9)}),
    ('Knuckles Mountain Range', {'budget': (300, 700), 'duration': (2, 5), 'group': (1, 4), 'adv': (9, 10), 'nat': (9, 10), 'bch': (1, 2), 'cul': (1, 3), 'nit': (1, 2), 'rel': (3, 6)}),
    ('Bentota', {'budget': (1000, 2500), 'duration': (3, 7), 'group': (2, 4), 'adv': (3, 6), 'nat': (5, 7), 'bch': (8, 10), 'cul': (3, 6), 'nit': (4, 7), 'rel': (9, 10)}),
    ('Anuradhapura', {'budget': (350, 750), 'duration': (2, 4), 'group': (1, 5), 'adv': (2, 5), 'nat': (4, 7), 'bch': (1, 2), 'cul': (9, 10), 'nit': (1, 2), 'rel': (5, 8)}),
    ('Trincomalee', {'budget': (450, 950), 'duration': (2, 6), 'group': (2, 5), 'adv': (5, 8), 'nat': (6, 8), 'bch': (8, 10), 'cul': (5, 8), 'nit': (3, 6), 'rel': (7, 9)})
]

destinations_db = {
    'Ella': {'cost': 65.0, 'min_d': 2, 'max_d': 5, 'bch': 1, 'adv': 9, 'nat': 10, 'cul': 4, 'nit': 6, 'rel': 7},
    'Mirissa': {'cost': 75.0, 'min_d': 2, 'max_d': 6, 'bch': 10, 'adv': 6, 'nat': 7, 'cul': 3, 'nit': 9, 'rel': 8},
    'Sigiriya': {'cost': 80.0, 'min_d': 1, 'max_d': 3, 'bch': 1, 'adv': 8, 'nat': 8, 'cul': 10, 'nit': 2, 'rel': 5},
    'Kandy': {'cost': 70.0, 'min_d': 2, 'max_d': 4, 'bch': 1, 'adv': 4, 'nat': 6, 'cul': 10, 'nit': 4, 'rel': 7},
    'Nuwara Eliya': {'cost': 85.0, 'min_d': 2, 'max_d': 4, 'bch': 1, 'adv': 5, 'nat': 9, 'cul': 6, 'nit': 3, 'rel': 9},
    'Arugam Bay': {'cost': 60.0, 'min_d': 3, 'max_d': 7, 'bch': 10, 'adv': 9, 'nat': 6, 'cul': 2, 'nit': 8, 'rel': 6},
    'Yala National Park': {'cost': 120.0, 'min_d': 1, 'max_d': 3, 'bch': 2, 'adv': 8, 'nat': 10, 'cul': 2, 'nit': 1, 'rel': 4},
    'Galle Fort': {'cost': 95.0, 'min_d': 1, 'max_d': 3, 'bch': 7, 'adv': 3, 'nat': 4, 'cul': 9, 'nit': 7, 'rel': 8},
    'Knuckles Mountain Range': {'cost': 50.0, 'min_d': 2, 'max_d': 4, 'bch': 1, 'adv': 10, 'nat': 10, 'cul': 2, 'nit': 1, 'rel': 5},
    'Bentota': {'cost': 150.0, 'min_d': 2, 'max_d': 5, 'bch': 9, 'adv': 4, 'nat': 6, 'cul': 4, 'nit': 5, 'rel': 10},
    'Anuradhapura': {'cost': 55.0, 'min_d': 2, 'max_d': 4, 'bch': 1, 'adv': 3, 'nat': 5, 'cul': 10, 'nit': 1, 'rel': 6},
    'Trincomalee': {'cost': 70.0, 'min_d': 2, 'max_d': 5, 'bch': 9, 'adv': 6, 'nat': 7, 'cul': 6, 'nit': 4, 'rel': 8}
}

data = []
for dest, cfg in PERSONAS:
    for _ in range(50):
        b = np.random.uniform(*cfg['budget'])
        dur = np.random.randint(cfg['duration'][0], cfg['duration'][1] + 1)
        grp = np.random.randint(cfg['group'][0], cfg['group'][1] + 1)
        adv = np.random.randint(cfg['adv'][0], cfg['adv'][1] + 1)
        nat = np.random.randint(cfg['nat'][0], cfg['nat'][1] + 1)
        bch = np.random.randint(cfg['bch'][0], cfg['bch'][1] + 1)
        cul = np.random.randint(cfg['cul'][0], cfg['cul'][1] + 1)
        nit = np.random.randint(cfg['nit'][0], cfg['nit'][1] + 1)
        rel = np.random.randint(cfg['rel'][0], cfg['rel'][1] + 1)
        feat = [
            (b - 100) / 2900,
            (dur - 1) / 13,
            (grp - 1) / 9,
            (bch - 1) / 4,
            (adv - 1) / 4,
            (nat - 1) / 4,
            (cul - 1) / 4,
            (nit - 1) / 4,
            (rel - 1) / 4
        ]
        data.append({
            'features': feat,
            'target': dest,
            'budget': b,
            'duration': dur,
            'prefs': [bch, adv, nat, cul, nit, rel]
        })

random.shuffle(data)
train_data = data[:420]
test_data = data[420:]

def calc_metrics(actuals, preds):
    classes = list(set(actuals))
    tp = {c: 0 for c in classes}
    fp = {c: 0 for c in classes}
    fn = {c: 0 for c in classes}
    correct = 0
    for a, p in zip(actuals, preds):
        if a == p:
            correct += 1
            tp[a] += 1
        else:
            fp[p] = fp.get(p, 0) + 1
            fn[a] += 1
    acc = correct / len(actuals)
    precs = [tp[c] / (tp[c] + fp.get(c, 0)) if (tp[c] + fp.get(c, 0)) > 0 else 0.0 for c in classes]
    recs = [tp[c] / (tp[c] + fn[c]) if (tp[c] + fn[c]) > 0 else 0.0 for c in classes]
    m_p = sum(precs) / len(classes)
    m_r = sum(recs) / len(classes)
    m_f1 = 2 * m_p * m_r / (m_p + m_r) if (m_p + m_r) > 0 else 0.0
    return acc, m_p, m_r, m_f1

# 1. Evaluate Decision Tree
def gini(y):
    if not y: return 0.0
    counts = {}
    for item in y: counts[item] = counts.get(item, 0) + 1
    return 1.0 - sum((cnt / len(y)) ** 2 for cnt in counts.values())

class SimpleDT:
    def __init__(self, max_depth, min_samples):
        self.max_depth = max_depth
        self.min_samples = min_samples
        self.tree = None

    def fit(self, X, y, depth=0):
        if depth >= self.max_depth or len(y) < self.min_samples or len(set(y)) == 1:
            counts = {}
            for item in y: counts[item] = counts.get(item, 0) + 1
            majority = max(counts.items(), key=lambda x: x[1])[0]
            probs = {k: v / len(y) for k, v in counts.items()}
            return {'leaf': True, 'class': majority, 'probs': probs}

        best_gain = -1
        best_split = None
        base_gini = gini(y)

        for feat_idx in range(len(X[0])):
            vals = sorted(list(set(row[feat_idx] for row in X)))
            for i in range(len(vals) - 1):
                thresh = (vals[i] + vals[i+1]) / 2.0
                left_idx = [j for j in range(len(X)) if X[j][feat_idx] <= thresh]
                right_idx = [j for j in range(len(X)) if X[j][feat_idx] > thresh]
                if not left_idx or not right_idx: continue
                g = base_gini - ((len(left_idx)/len(y))*gini([y[j] for j in left_idx]) + (len(right_idx)/len(y))*gini([y[j] for j in right_idx]))
                if g > best_gain:
                    best_gain = g
                    best_split = (feat_idx, thresh, left_idx, right_idx)

        if best_split is None or best_gain <= 1e-5:
            counts = {}
            for item in y: counts[item] = counts.get(item, 0) + 1
            majority = max(counts.items(), key=lambda x: x[1])[0]
            probs = {k: v / len(y) for k, v in counts.items()}
            return {'leaf': True, 'class': majority, 'probs': probs}

        feat_idx, thresh, left_idx, right_idx = best_split
        return {
            'leaf': False,
            'feat': feat_idx,
            'thresh': thresh,
            'left': self.fit([X[j] for j in left_idx], [y[j] for j in left_idx], depth + 1),
            'right': self.fit([X[j] for j in right_idx], [y[j] for j in right_idx], depth + 1)
        }

    def predict_one(self, node, x):
        if node['leaf']:
            return node['class'], node['probs']
        if x[node['feat']] <= node['thresh']:
            return self.predict_one(node['left'], x)
        return self.predict_one(node['right'], x)

print("=== DECISION TREE EVALUATIONS ===")
dt_models = {}
for depth in [3, 5, 7, 10]:
    for ms in [2, 5]:
        dt = SimpleDT(depth, ms)
        tree = dt.fit([d['features'] for d in train_data], [d['target'] for d in train_data])
        dt_models[(depth, ms)] = (dt, tree)
        preds = [dt.predict_one(tree, d['features'])[0] for d in test_data]
        acc, p, r, f1 = calc_metrics([d['target'] for d in test_data], preds)
        print(f"Decision Tree | MaxDepth={depth:2d}, MinSamples={ms:2d} | Accuracy: {acc:.4f} | Precision: {p:.4f} | Recall: {r:.4f} | F1: {f1:.4f}")

# 2. Evaluate KNN
print("\n=== KNN EVALUATIONS ===")
def knn_predict(test_sample, k):
    dists = []
    for tr in train_data:
        d = np.linalg.norm(np.array(test_sample['features']) - np.array(tr['features']))
        dists.append((d, tr['target']))
    dists.sort(key=lambda x: x[0])
    top_k = dists[:k]
    votes = {}
    for d, target in top_k:
        w = 1.0 / (d + 1e-4)
        votes[target] = votes.get(target, 0.0) + w
    return max(votes.items(), key=lambda x: x[1])[0], votes

for k in [1, 3, 5, 7, 9]:
    preds = [knn_predict(d, k)[0] for d in test_data]
    acc, p, r, f1 = calc_metrics([d['target'] for d in test_data], preds)
    print(f"KNN | K={k:2d} | Accuracy: {acc:.4f} | Precision: {p:.4f} | Recall: {r:.4f} | F1: {f1:.4f}")

# 3. Evaluate Hybrid Configurations
print("\n=== HYBRID EVALUATIONS ===")
best_dt, best_tree = dt_models[(7, 2)]

def cos_sim(t_p, d_p):
    t = np.array(t_p) / 5.0
    d = np.array(d_p) / 10.0
    return float(np.dot(t, d) / (np.linalg.norm(t) * np.linalg.norm(d)))

def budget_comp(budget, duration, daily_cost):
    est = daily_cost * duration
    if est <= budget:
        return 0.75 + 0.25 * ((budget - est) / budget)
    return max(0.0, 0.60 - 0.60 * ((est - budget) / budget))

def dur_comp(dur, min_d, max_d):
    if min_d <= dur <= max_d: return 1.0
    if dur < min_d: return max(0.2, 1.0 - 0.25*(min_d - dur))
    return max(0.3, 1.0 - 0.15*(dur - max_d))

hybrid_weights = [
    ("Balanced (30% DT, 25% KNN, 25% Pref, 10% Budg, 10% Dur)", (0.30, 0.25, 0.25, 0.10, 0.10)),
    ("DT-Heavy (50% DT, 15% KNN, 15% Pref, 10% Budg, 10% Dur)", (0.50, 0.15, 0.15, 0.10, 0.10)),
    ("KNN-Heavy (15% DT, 50% KNN, 15% Pref, 10% Budg, 10% Dur)", (0.15, 0.50, 0.15, 0.10, 0.10)),
    ("Preference-Heavy (15% DT, 15% KNN, 50% Pref, 10% Budg, 10% Dur)", (0.15, 0.15, 0.50, 0.10, 0.10))
]

for name, (w_dt, w_knn, w_pref, w_budg, w_dur) in hybrid_weights:
    preds = []
    for test_sample in test_data:
        dt_class, dt_probs = best_dt.predict_one(best_tree, test_sample['features'])
        _, knn_votes = knn_predict(test_sample, 5)
        max_knn = max(knn_votes.values()) if knn_votes else 1.0
        
        candidates = []
        for dest_name, dest_info in destinations_db.items():
            s_dt = 0.90 if dest_name == dt_class else dt_probs.get(dest_name, 0.10)
            s_knn = (knn_votes.get(dest_name, 0.0) / max_knn) if max_knn > 0 else 0.40
            d_p = [dest_info['bch'], dest_info['adv'], dest_info['nat'], dest_info['cul'], dest_info['nit'], dest_info['rel']]
            s_pref = cos_sim(test_sample['prefs'], d_p)
            s_budg = budget_comp(test_sample['budget'], test_sample['duration'], dest_info['cost'])
            s_dur = dur_comp(test_sample['duration'], dest_info['min_d'], dest_info['max_d'])
            
            final_score = (w_dt * s_dt) + (w_knn * s_knn) + (w_pref * s_pref) + (w_budg * s_budg) + (w_dur * s_dur)
            candidates.append((final_score, s_pref, dest_name))
        
        candidates.sort(key=lambda x: (x[0], x[1]), reverse=True)
        preds.append(candidates[0][2])
    
    acc, p, r, f1 = calc_metrics([d['target'] for d in test_data], preds)
    print(f"Hybrid | {name} | Accuracy: {acc:.4f} | Precision: {p:.4f} | Recall: {r:.4f} | F1: {f1:.4f}")
