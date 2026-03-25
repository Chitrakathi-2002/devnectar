function MyRecruits() {
    const [recruits, setRecruits] = React.useState([]);

    React.useEffect(() => {
        fetch('/employee/my-recruits')
            .then(res => res.json())
            .then(data => setRecruits(data))
            .catch(err => console.error("Error fetching recruits: ", err));
    }, []);

    const approveIntern = (id) => {
        fetch(`/employee/approvals/${id}`, { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                alert(data.message);
                setRecruits(recruits.filter(r => r.id !== id));
            });
    };

    return (
        <div style={{marginTop: "2rem"}}>
            <h3 style={{marginBottom: "1rem"}}>My Recruits (Pending Approval)</h3>
            {recruits.length === 0 ? <p style={{color: "var(--text-secondary)"}}>No pending recruits.</p> : (
                <table className="data-table">
                    <thead><tr><th>Name</th><th>University</th><th>Duration</th><th>Action</th></tr></thead>
                    <tbody>
                        {recruits.map(r => (
                            <tr key={r.id}>
                                <td>{r.fullName} ({r.username})</td>
                                <td>{r.university}</td>
                                <td>{r.internshipDuration}</td>
                                <td><button className="btn btn-primary" onClick={() => approveIntern(r.id)}>Vouch & Approve</button></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

const root = ReactDOM.createRoot(document.getElementById('react-employee-root'));
root.render(<MyRecruits />);
