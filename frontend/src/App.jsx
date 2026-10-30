import { useEffect, useState } from 'react';

// On your laptop this is localhost. On Vercel, VITE_API_URL is set to the Render address.
const API = (import.meta.env.VITE_API_URL || 'http://localhost:8080') + '/api/applications';

const BLANK = { company: '', role: '', location: '', source: '', appliedOn: '', interviewDate: '', notes: '' };

export default function App() {
  const [apps, setApps] = useState([]);
  const [stats, setStats] = useState({});
  const [form, setForm] = useState(BLANK);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  // Runs once after the first render, then never again (empty dependency array).
  useEffect(() => {
    load();
  }, []);

  async function load() {
    try {
      const [listRes, statsRes] = await Promise.all([fetch(API), fetch(API + '/stats')]);
      if (!listRes.ok) throw new Error('Could not reach the API.');
      setApps(await listRes.json());
      setStats(await statsRes.json());
      setError('');
    } catch (e) {
      setError(e.message);
    }
    setLoading(false);
  }

  // One handler for every text input, keyed by the input's name attribute.
  function onChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function addApplication(e) {
    e.preventDefault();
    // An empty date box means "no date", so send null instead of an empty string.
    const body = { ...form, interviewDate: form.interviewDate || null };
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (res.ok) {
      setForm(BLANK);
      load();
    } else {
      setError(await readError(res));
    }
  }

  async function moveStatus(id, status) {
    if (!status) return;
    const res = await fetch(`${API}/${id}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status })
    });
    if (res.ok) load();
    else setError(await readError(res));
  }

  async function remove(id) {
    const res = await fetch(`${API}/${id}`, { method: 'DELETE' });
    if (res.ok) load();
    else setError(await readError(res));
  }

  // The API always returns the same error shape, so one reader handles every failure.
  async function readError(res) {
    const body = await res.json().catch(() => null);
    if (body && body.fieldErrors) return Object.values(body.fieldErrors).join(' ');
    return (body && body.message) || `Request failed with ${res.status}.`;
  }

  return (
    <div className="wrap">
      <h1>Job Application Tracker</h1>
      <p className="sub">Applied &rarr; Assessment &rarr; Interview &rarr; Offer</p>

      {error && <div className="msg error">{error}</div>}

      <div className="stats">
        {['APPLIED', 'ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED', 'TOTAL'].map((key) => (
          <div className="stat" key={key}>
            <b>{stats[key] ?? 0}</b>
            <span>{key}</span>
          </div>
        ))}
      </div>

      <form onSubmit={addApplication}>
        <label><span>Company</span><input name="company" value={form.company} onChange={onChange} required /></label>
        <label><span>Role</span><input name="role" value={form.role} onChange={onChange} required /></label>
        <label><span>Location</span><input name="location" value={form.location} onChange={onChange} /></label>
        <label><span>Source</span><input name="source" value={form.source} onChange={onChange} placeholder="Campus, LinkedIn..." /></label>
        <label><span>Applied on</span><input name="appliedOn" value={form.appliedOn} onChange={onChange} type="date" required /></label>
        <label><span>Interview date</span><input name="interviewDate" value={form.interviewDate} onChange={onChange} type="date" /></label>
        <label><span>Notes</span><input name="notes" value={form.notes} onChange={onChange} /></label>
        <button className="primary" type="submit">Add</button>
      </form>

      {loading ? (
        <p className="empty">Loading. The free server can take about a minute to wake up.</p>
      ) : apps.length === 0 ? (
        <p className="empty">Nothing tracked yet. Add your first application above.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Company</th>
              <th>Role</th>
              <th>Applied</th>
              <th>Interview</th>
              <th>Status</th>
              <th>Move to</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {apps.map((app) => (
              <tr key={app.id}>
                <td>{app.company}</td>
                <td>
                  {app.role}
                  {app.notes && <div className="note">{app.notes}</div>}
                </td>
                <td>{app.appliedOn}</td>
                <td>{app.interviewDate || '-'}</td>
                <td><span className={'pill ' + app.status}>{app.status}</span></td>
                <td>
                  {/* The API tells the UI which moves are legal, so the rule lives in one place. */}
                  {app.allowedNext.length === 0 ? (
                    <span className="sub">final</span>
                  ) : (
                    <select value="" onChange={(e) => moveStatus(app.id, e.target.value)}>
                      <option value="">choose</option>
                      {app.allowedNext.map((next) => (
                        <option key={next} value={next}>{next}</option>
                      ))}
                    </select>
                  )}
                </td>
                <td><button className="link" onClick={() => remove(app.id)}>delete</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
